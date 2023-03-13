package com.pdfs.server;

import java.io.*;
import java.util.Map;

public class HttpHandler {
    String usage = """
            <body>
                Welcome To PDFS V1.0! Your can click <a href='/fsapi/webls/'>here</a> to Quick Start
                <br></br>
                PDFS Using <a href="https://github.com/fightinggg/pdfs-data-githubapi">Github Repo</a> To Stoage Files, Every File In Github Has Bean Encrypting
                <br></br>
                PDFS source code in <a href='https://github.com/fightinggg/pdfs'>here</a>
            </body>

            """;


    FsHandler fsHandler;

    public HttpHandler(Map<String, String> config) {
        this.fsHandler = new FsHandler(config);
    }

    HttpRsp httpHandler(String url, String method, InputStream inputStream) throws IOException {
        if (url.contains("//")) {
            return new HttpRsp(400, "Bad URL: " + url);
        }
        if (method.equals("GET") && url.startsWith("/static/")) {
            return getStaticFiles(url);
        } else if (url.startsWith("/fsapi/")) {
            return fsHandler.httpHandler(url, method, inputStream);
        } else {
            return new HttpRsp(200, usage);
        }
    }

    private HttpRsp getStaticFiles(String url) throws IOException {
        if (url.contains("..")) {
            return new HttpRsp(403, "url could not contain '..' ");

        } else {
            try {
                return new HttpRsp(200, new FileInputStream(url));
            } catch (Exception e) {
                return new HttpRsp(404, "could not found " + url);
            }
        }
    }


}
