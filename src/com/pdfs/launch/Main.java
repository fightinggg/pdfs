package com.pdfs.launch;

import com.pdfs.server.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.pdfs.utils.ParamParse.parse;

@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {

        Map<String, String> config = parse(args);

        HttpServer server = new HttpServer(config);
        server.start();
    }


}
