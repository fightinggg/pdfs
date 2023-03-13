package com.pdfs.basicnetfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.DownloadUrl;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 七牛云需要绑定一个域名
 *
 */
public class QiniuBasicNetFsImpl extends ValidBasicNetFsAbstract {
    Logger log = LoggerFactory.getLogger(QiniuBasicNetFsImpl.class);
    String accessKey;
    String secretKey;
    String bucket;

    public QiniuBasicNetFsImpl(String accessKey, String secretKey, String bucket) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
    }

    @Override
    public byte[] readValid(String fileName) throws IOException {
        // domain   下载 domain, eg: qiniu.com【必须】
// useHttps 是否使用 https【必须】
// key      下载资源在七牛云存储的 key【必须】
        DownloadUrl url = new DownloadUrl("qiniu.com", false, fileName);
//        url.setAttname(attname) // 配置 attname
//                .setFop(fop) // 配置 fop
//                .setStyle(style, styleSeparator, styleParam) // 配置 style
        String urlString = url.buildURL();
        log.info(urlString);
        throw new FileNotFoundException();
    }

    @Override
    public void writeValid(String fileName, byte[] data) throws IOException {
//构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region2());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
//...其他参数参考类注释

        UploadManager uploadManager = new UploadManager(cfg);
//...生成上传凭证，然后准备上传


//默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = fileName;

        byte[] uploadBytes = data;
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        try {
            Response response = uploadManager.put(uploadBytes, key, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new ObjectMapper().readValue(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }

    }

    @Override
    public void deleteValid(String fileName) throws IOException {

    }
}
