package com.pdfs;

import com.pdfs.server.HttpServer;

public class Main {

    public static void main(String[] args) throws Exception{
        HttpServer server = new HttpServer(8081);// 8081为启动端口
        server.start();
    }
}
