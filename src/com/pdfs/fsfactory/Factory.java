package com.pdfs.fsfactory;

import com.pdfs.basicnetfs.*;
import com.pdfs.extendnetfs.ExtendNetFsAdepr;
import com.pdfs.extendnetfs.ExtendableNetFs;
import com.pdfs.extendnetfs.encryptionfs.AesEncryptionFsImpl;
import com.pdfs.extendnetfs.encryptionfs.NoEncryptionFsImpl;
import com.pdfs.normalfs.NormalFs;
import com.pdfs.extendnetfs.signfs.Md5SignFsImpl;
import com.pdfs.reliable.PolinDistributeFs;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class Factory {

    public static NormalFs getNormalFs(Map<String, String> config) {
        return new PolinDistributeFs(config);

    }

    public static ExtendableNetFs getExtendNetFs(Map<String, String> config) {
        BasicNetFs basicNetFs;

        if (Objects.equals(config.get("type"), "local")) {
            basicNetFs = new LocalFileSystemExtendableNetFsImpl(config);
        } else if (Objects.equals(config.get("type"), "git")) {
            basicNetFs = new JGitRepoExtendableNetFsImpl("".getBytes(StandardCharsets.UTF_8), "git@github.com:fightinggg/pdfs-data.git");
        } else if (Objects.equals(config.get("type"), "system_git")) {
            String localRepo = null;
            try {
                localRepo = new File("../pdfs_systemgitfs").getCanonicalPath();
            } catch (IOException e) {
                localRepo = "./pdfs_systemgitfs";
            }
            basicNetFs = new SystemGitRepoExtendableNetFsImpl(localRepo, "git@github.com:fightinggg/pdfs-data.git", "");
        } else if (Objects.equals(config.get("type"), "qiniu")) {
            basicNetFs = new QiniuExtendableNetFsImpl(config.get("qiniu_accessKey"), config.get("qiniu_secretKey"), config.get("qiniu_bucket"));
        } else if (Objects.equals(config.get("type"), "github_api")) {
            basicNetFs = new GithubApiExtendableNetFsImpl(config.get("github_token"), config.get("github_username"), config.get("github_reponame"));
        } else {
            throw new RuntimeException("FS ERR");
        }

        ExtendableNetFs extendableNetFs = new ExtendNetFsAdepr(basicNetFs);

        //extendableNetFs = new CacheFs(extendableNetFs);


        // TODO ADD MD5
//        extendableNetFs = new Md5SignFsImpl(extendableNetFs);

        String key = processKey(config);

        extendableNetFs = key.length() == 0 ? new NoEncryptionFsImpl(extendableNetFs) : new AesEncryptionFsImpl(extendableNetFs, key.getBytes());


        return extendableNetFs;
    }

    @NotNull
    private static String processKey(Map<String, String> config) {

        if (!config.containsKey("key")) {
            throw new RuntimeException("valid config: you should set a key");
        }

        String key = config.get("key");
        while (!Objects.equals(key, "") && key.length() < 32) {
            key = key + " ";
        }
        return key;
    }
}
