package com.pdfs.utils;

import com.pdfs.normalfs.PdfsFileInputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;

public class AES {

    public static PdfsFileInputStream decode(PdfsFileInputStream data, byte[] key) {
        try {
            return PdfsFileInputStream.fromBytes(decode(data.readAllBytes(), key));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static PdfsFileInputStream encode(PdfsFileInputStream data, byte[] key) {
        try {
            return PdfsFileInputStream.fromBytes(encode(data.readAllBytes(), key));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static byte[] decode(byte[] data, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encode(byte[] data, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
