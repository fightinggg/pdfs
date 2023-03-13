package com.pdfs.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRsp {
    int status;
    InputStream body;
    public Map<String,String> headers = new HashMap<>();

    public HttpRsp(int status, InputStream body) {
        this.status = status;
        this.body = body;
    }

    public HttpRsp(int status, String body) {
        this.status = status;
        this.body = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
    }


    public HttpRsp(int status, byte[] body) {
        this.status = status;
        this.body = new ByteArrayInputStream(body);
    }
}
