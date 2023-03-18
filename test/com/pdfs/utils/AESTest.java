package com.pdfs.utils;

import com.pdfs.normalfs.PdfsFileInputStream;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AESTest {

    private final byte[] randomBytes(int len) {
        byte[] res = new byte[len];
        Random random = new Random();
        random.nextBytes(res);
        return res;
    }

    @Test
    public void f() throws IOException {
        List<byte[]> tests = List.of(
                randomBytes(0),
                randomBytes(1),
                randomBytes(2),
                randomBytes(3),
                randomBytes(4),
                randomBytes(15),
                randomBytes(16),
                randomBytes(17),
                randomBytes(31),
                randomBytes(32),
                randomBytes(33),
                randomBytes(100),
                randomBytes(100),
                randomBytes(100),
                randomBytes(100),
                randomBytes(100),
                randomBytes(100),
                randomBytes(100),
                randomBytes(100),
                randomBytes(100),
                randomBytes(1000),
                randomBytes(100000),
                randomBytes(1000000),
                randomBytes(1000000),
                randomBytes(1000000)
        );
        byte[] key = "0123456789012345".getBytes(StandardCharsets.UTF_8);

        for (byte[] test : tests) {
            try {
                PdfsFileInputStream pdfsFileInputStream = AES.encodePerSize(PdfsFileInputStream.fromBytes(test), key);
                byte[] bytes = pdfsFileInputStream.readAllBytes();
                assert bytes.length == pdfsFileInputStream.getRemainSize();

                PdfsFileInputStream pdfsFileInputStream1 = AES.decodePerSize(PdfsFileInputStream.fromBytes(bytes), key);
                byte[] bytes1 = pdfsFileInputStream1.readAllBytes();
                assert bytes1.length == pdfsFileInputStream1.getRemainSize();
                assert Arrays.equals(bytes1, test);
                System.out.println("OK " + test.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}