package com.pdfs.utils;

import com.pdfs.normalfs.PdfsFileInputStream;

import java.io.IOException;

public class Base64 {

    public static byte[] encode(byte[] data) {
        return java.util.Base64.getEncoder().encode(data);
    }

    public static byte[] decode(byte[] data) {
        return java.util.Base64.getDecoder().decode(data);
    }

    public static PdfsFileInputStream encode(PdfsFileInputStream pdfsFileInputStream) {
        byte[] src = new byte[0];
        try {
            src = pdfsFileInputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return PdfsFileInputStream.fromBytes(java.util.Base64.getEncoder().encode(src));
    }

    public static PdfsFileInputStream decode(PdfsFileInputStream pdfsFileInputStream) {
        byte[] bytes = new byte[0];
        try {
            bytes = pdfsFileInputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return PdfsFileInputStream.fromBytes(java.util.Base64.getDecoder().decode(bytes));
    }
}
