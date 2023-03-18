package com.pdfs.server.fshander;

import com.pdfs.normalfs.NormalFs;
import com.pdfs.normalfs.PdfsFileInputStream;
import com.pdfs.server.HttpRsp;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class FsReadHandler {

    Logger log = LoggerFactory.getLogger(FsReadHandler.class);

    NormalFs fs;

    public FsReadHandler(NormalFs fs) {
        this.fs = fs;
    }

    public HttpRsp readHandler(String url, String method, InputStream inputStream) throws IOException {
        String path = url.substring("/fsapi/read/".length() - 1);
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);

        try {
            PdfsFileInputStream read = fs.read(path, 0, 999999999999L);
            HttpRsp rsp = new HttpRsp(200, read);
            rsp.headers.put("Content-Type", "text/plain; charset=utf-8");
            rsp.headers.put("Content-Length", String.valueOf(read.getFileSize()));
            return rsp;
        } catch (FileNotFoundException e) {
            return new HttpRsp(404, "Could Not Found " + path);
        } catch (Exception e) {
            log.error("", e);
            return new HttpRsp(500, "Server ERROR ");
        }
    }

}
