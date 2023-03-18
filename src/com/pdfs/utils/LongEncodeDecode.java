package com.pdfs.utils;

import io.netty.buffer.Unpooled;

public class LongEncodeDecode {
    public static byte[] encode(long x) {
        byte[] res = new byte[8];
        Unpooled.buffer().writeLong(x).readBytes(res);
        return res;
    }

    public static long decode(byte[] x) {
        return Unpooled.buffer().writeBytes(x).readLong();
    }
}
