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
        List<Integer> tests = List.of(
                0,
                1,
                2,
                3,
                4,
                15,
                16,
                17,
                31,
                32,
                33,
                100,
                100,
                100,
                100,
                100,
                100,
                100,
                100,
                100,
                1000,
                100000,
                1000000,
                1000000,
                1000000
        );
        byte[] key = "0123456789012345".getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < 10; i++) {
            for (int testSize : tests) {
                try {
                    byte[] test = randomBytes(testSize);
                    PdfsFileInputStream pdfsFileInputStream = AES.encodePerSize(PdfsFileInputStream.fromBytes(test), key);
                    byte[] bytes = pdfsFileInputStream.readAllBytes();

                    PdfsFileInputStream pdfsFileInputStream1 = AES.decodePerSize(PdfsFileInputStream.fromBytes(bytes), key);
                    byte[] bytes1 = pdfsFileInputStream1.readAllBytes();
                    assert Arrays.equals(bytes1, test);
                    System.out.println("OK " + test.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}