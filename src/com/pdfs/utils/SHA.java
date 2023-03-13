package com.pdfs.utils;


import org.eclipse.jgit.util.sha1.SHA1;

public class SHA {
    public static byte[] encode(byte[] bytes) {
        SHA1 sha1 = SHA1.newInstance();
        sha1.update(bytes);
        return sha1.digest();
    }
}
