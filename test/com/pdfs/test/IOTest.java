package com.pdfs.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfs.reliable.PolinDistributeFsCluster;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class IOTest {
    @Test
    public void f3() throws IOException {
        HashMap<String, String> config = new HashMap<>();
        config.put("group", "local");
        config.put("name", "local_1");
        config.put("type", "local");
        config.put("key", "wow! pdfs!");
        config.put("local_path", "./local_1");

        PolinDistributeFsCluster polinDistributeFsCluster = new PolinDistributeFsCluster(config);

        List<PolinDistributeFsCluster.NetFsDisk> disks = polinDistributeFsCluster.getDisks();

        System.out.println(new ObjectMapper().writeValueAsString(disks));


        for (int i = 0; i < 5; i++) {
            config = new HashMap<>();
            config.put("group", "local");
            config.put("name", "local_2");
            config.put("type", "local");
            config.put("key", "wow! pdfs!");
            config.put("local_path", "./local_1");
            polinDistributeFsCluster.addConfig(config);
        }

        System.out.println(new ObjectMapper().writeValueAsString(disks));



    }


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
//        ExtendableNetFs extendableNetFs = new LocalFileSystemExtendableNetFsImpl("D:\\src\\pdfs\\localfs");
//        SignFs signFs = new Md5SignFsImpl(extendableNetFs);
//        EncryptionFs encryptionFs = new AesEncryptionFsImpl(signFs, "wow! pdsf! good!".getBytes());
//
//
//        String fileName = "pdfs_test.bin";
//        String data = "12345678911";
//
//        encryptionFs.write(fileName, data.getBytes(StandardCharsets.UTF_8));
//        byte[] read = encryptionFs.read(fileName);
//
//        assert data.endsWith(new String(read));


    }


    @Test
    public void f2() throws IOException {
//        String key = "wow! pdsf! good!";
//
//        ExtendableNetFs extendableNetFs = new LocalFileSystemExtendableNetFsImpl("D:\\src\\pdfs\\localfs");
//        SignFs signFs = new Md5SignFsImpl(extendableNetFs);
//        EncryptionFs encryptionFs = new AesEncryptionFsImpl(signFs, key.getBytes());
//        FileNormalFsImpl directFileNormalFs = new FileNormalFsImpl(key.getBytes(StandardCharsets.UTF_8), encryptionFs);
//
//
//        System.out.println(directFileNormalFs.ls(""));
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
