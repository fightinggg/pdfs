package com.pdfs.fs;

import com.pdfs.basicnetfs.BasicNetFs;
import com.pdfs.basicnetfs.GithubApiBasicNetFsImpl;
import com.pdfs.basicnetfs.JGitRepoBasicNetFsImpl;
import com.pdfs.basicnetfs.LocalFileSystemBasicNetFsImpl;
import com.pdfs.basicnetfs.QiniuBasicNetFsImpl;
import com.pdfs.basicnetfs.SystemGitRepoBasicNetFsImpl;
import com.pdfs.encryptionfs.AesEncryptionFsImpl;
import com.pdfs.encryptionfs.EncryptionFs;
import com.pdfs.encryptionfs.NoEncryptionFsImpl;
import com.pdfs.normalfs.FileNormalFsImpl;
import com.pdfs.normalfs.NormalFs;
import com.pdfs.signfs.Md5SignFsImpl;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class Factory {
    public static NormalFs getFs(Map<String, String> config) {

        BasicNetFs basicNetFs = getBasicNetFs(config);

        return getFileNormalFs(config, basicNetFs);

    }

    private static BasicNetFs getBasicNetFs(Map<String, String> config) {
        BasicNetFs basicNetFs;

        if (Objects.equals(config.get("type"), "local")) {
            basicNetFs = new LocalFileSystemBasicNetFsImpl("./localfs");
        } else if (Objects.equals(config.get("type"), "git")) {
            basicNetFs = new JGitRepoBasicNetFsImpl("".getBytes(StandardCharsets.UTF_8), "git@github.com:fightinggg/pdfs-data.git");
        } else if (Objects.equals(config.get("type"), "system_git")) {
            String localRepo = null;
            try {
                localRepo = new File("../pdfs_systemgitfs").getCanonicalPath();
            } catch (IOException e) {
                localRepo = "./pdfs_systemgitfs";
            }
            basicNetFs = new SystemGitRepoBasicNetFsImpl(localRepo, "git@github.com:fightinggg/pdfs-data.git", "");
        } else if (Objects.equals(config.get("type"), "qiniu")) {
            basicNetFs = new QiniuBasicNetFsImpl(config.get("qiniu_accessKey"), config.get("qiniu_secretKey"), config.get("qiniu_bucket"));
        } else if (Objects.equals(config.get("type"), "github_api")) {
            basicNetFs = new GithubApiBasicNetFsImpl(config.get("github_token"), config.get("github_username"), config.get("github_reponame"));
        } else {
            throw new RuntimeException("FS ERR");
        }
        return basicNetFs;
    }

    private static FileNormalFsImpl getFileNormalFs(Map<String, String> config, BasicNetFs basicNetFs) {
        basicNetFs = new Md5SignFsImpl(basicNetFs);
        basicNetFs = new Md5SignFsImpl(basicNetFs);

        String key = processKey(config);

        EncryptionFs encryptionFs = key.length() == 0 ? new NoEncryptionFsImpl(basicNetFs) : new AesEncryptionFsImpl(basicNetFs, key.getBytes());

        return new FileNormalFsImpl(encryptionFs);
    }

    @NotNull
    private static String processKey(Map<String, String> config) {
        String key = config.get("key");
        while (key.length() < 32) {
            key = key + " ";
        }
        return key;
    }
}
