package com.pdfs.fs;

import com.pdfs.basicnetfs.BasicNetFs;
import com.pdfs.basicnetfs.JGitRepoBasicNetFsImpl;
import com.pdfs.basicnetfs.LocalFileSystemBasicNetFsImpl;
import com.pdfs.basicnetfs.SystemGitRepoBasicNetFsImpl;
import com.pdfs.encryptionfs.AesEncryptionFsImpl;
import com.pdfs.encryptionfs.EncryptionFs;
import com.pdfs.normalfs.DirectFileNormalFsImpl;
import com.pdfs.normalfs.NormalFs;
import com.pdfs.signfs.Md5SignFsImpl;
import com.pdfs.signfs.SignFs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class Factory {
    public static NormalFs getFs(String key, String type, Map<String, String> config) {
        if (Objects.equals(type, "local")) {
            while (key.length() < 32) {
                key = key + " ";
            }

            BasicNetFs basicNetFs = new LocalFileSystemBasicNetFsImpl("./localfs");
            SignFs signFs = new Md5SignFsImpl(basicNetFs);
            EncryptionFs encryptionFs = new AesEncryptionFsImpl(signFs, key.getBytes());
            return new DirectFileNormalFsImpl(key.getBytes(StandardCharsets.UTF_8), encryptionFs);
        } else if (Objects.equals(type, "git")) {
            while (key.length() < 32) {
                key = key + " ";
            }

            BasicNetFs basicNetFs = new JGitRepoBasicNetFsImpl("".getBytes(StandardCharsets.UTF_8), "git@github.com:fightinggg/pdfs-data.git");
            SignFs signFs = new Md5SignFsImpl(basicNetFs);
            EncryptionFs encryptionFs = new AesEncryptionFsImpl(signFs, key.getBytes());
            return new DirectFileNormalFsImpl(key.getBytes(StandardCharsets.UTF_8), encryptionFs);
        } else if (Objects.equals(type, "system_git")) {
            while (key.length() < 32) {
                key = key + " ";
            }

            String localRepo = null;
            try {
                localRepo = new File("../pdfs_systemgitfs").getCanonicalPath();
            } catch (IOException e) {
                localRepo = "./pdfs_systemgitfs";
            }

            BasicNetFs basicNetFs = new SystemGitRepoBasicNetFsImpl(localRepo, "git@github.com:fightinggg/pdfs-data.git", "");
            SignFs signFs = new Md5SignFsImpl(basicNetFs);
            EncryptionFs encryptionFs = new AesEncryptionFsImpl(signFs, key.getBytes());
            return new DirectFileNormalFsImpl(key.getBytes(StandardCharsets.UTF_8), encryptionFs);
        }
        throw new RuntimeException("FS ERR");
    }
}
