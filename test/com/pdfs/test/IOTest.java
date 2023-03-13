package com.pdfs.test;

import com.pdfs.basicnetfs.BasicNetFs;
import com.pdfs.basicnetfs.LocalFileSystemBasicNetFsImpl;
import com.pdfs.encryptionfs.AesEncryptionFsImpl;
import com.pdfs.encryptionfs.EncryptionFs;
import com.pdfs.fs.Factory;
import com.pdfs.normalfs.FileNormalFsImpl;
import com.pdfs.normalfs.NormalFs;
import com.pdfs.signfs.Md5SignFsImpl;
import com.pdfs.signfs.SignFs;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class IOTest {


    @Test
    public void f0() throws IOException {
//        NormalFs fs = Factory.getFs("123", "system_git", new HashMap<>());
//
//        String fileName = "/pdfs_test.bin";
//        String data = "12345678911";
//
////        fs.write(fileName, 0, new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
//        byte[] read = fs.read(fileName, 0, 1000).readAllBytes();
//
//        assert data.endsWith(new String(read));
//
//
//        fileName = "/a/b/c/d.bin";
//        data = "abcde";
//
////        fs.write(fileName, 0, new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
//        read = fs.read(fileName, 0, 1000).readAllBytes();
//
//        assert data.endsWith(new String(read));


    }

    @Test
    public void f1() throws IOException {
        BasicNetFs basicNetFs = new LocalFileSystemBasicNetFsImpl("D:\\src\\pdfs\\localfs");
        SignFs signFs = new Md5SignFsImpl(basicNetFs);
        EncryptionFs encryptionFs = new AesEncryptionFsImpl(signFs, "wow! pdsf! good!".getBytes());


        String fileName = "pdfs_test.bin";
        String data = "12345678911";

        encryptionFs.write(fileName, data.getBytes(StandardCharsets.UTF_8));
        byte[] read = encryptionFs.read(fileName);

        assert data.endsWith(new String(read));


    }


    @Test
    public void f2() throws IOException {
        String key = "wow! pdsf! good!";

        BasicNetFs basicNetFs = new LocalFileSystemBasicNetFsImpl("D:\\src\\pdfs\\localfs");
        SignFs signFs = new Md5SignFsImpl(basicNetFs);
        EncryptionFs encryptionFs = new AesEncryptionFsImpl(signFs, key.getBytes());
        FileNormalFsImpl directFileNormalFs = new FileNormalFsImpl(key.getBytes(StandardCharsets.UTF_8), encryptionFs);


        System.out.println(directFileNormalFs.ls(""));
//
//        directFileNormalFs.write("/a/b/c/d/e.txt", 0, new ByteArrayInputStream("123".getBytes(StandardCharsets.UTF_8)));
//
//        directFileNormalFs.write("/a/b/c/d/e", 0, new ByteArrayInputStream("123".getBytes(StandardCharsets.UTF_8)));
//
//        System.out.println(new String(directFileNormalFs.read("/a/b/c/d/e", 0, 100).readAllBytes()));
//
//        directFileNormalFs.write("/a/b/c/d", 0, new ByteArrayInputStream("ab".getBytes(StandardCharsets.UTF_8)));
//
//        System.out.println(new String(directFileNormalFs.read("/a/b/c/d", 0, 100).readAllBytes()));
//
//
//        System.out.println(directFileNormalFs.ls(""));
//        System.out.println(directFileNormalFs.ls("/a"));
//        System.out.println(directFileNormalFs.ls("/a/b"));
//        System.out.println(directFileNormalFs.ls("/a/b/c"));
//        System.out.println(directFileNormalFs.ls("/a/b/c/d"));

    }
}
