package com.pdfs.utils;

import com.pdfs.normalfs.PdfsFileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Queue;

public class Base64 {

    public static byte[] encode(byte[] data) {
        return java.util.Base64.getEncoder().encode(data);
    }

    public static byte[] decode(byte[] data) {
        return java.util.Base64.getDecoder().decode(data);
    }

}
