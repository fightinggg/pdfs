package com.pdfs.utils;

import org.junit.Test;

import java.util.List;

public class LongEncodeDecodeTest  {

    @Test
    public void f() {
        List<Long> tests = List.of(1L, 2L, 3L, 12321312312312L, 3213123123L, 90249382409184012L, -123123213L, 0L, -123123L);
        for (Long test : tests) {
            byte[] encode = LongEncodeDecode.encode(test);
            assert encode.length == 8;
            long decode = LongEncodeDecode.decode(encode);
            assert decode == test;
        }
    }
}