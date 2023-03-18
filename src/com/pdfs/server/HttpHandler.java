package com.pdfs.server;

import com.pdfs.utils.Base64;
import io.netty.handler.codec.http.HttpHeaders;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class HttpHandler {
    String usage = """
            <body>
                Welcome To PDFS V1.0! Your can click <a href='/fsapi/webls/'>here</a> to Quick Start
                <br></br>
                Click <a href='/fsapi/webcluster'>here</a> to manage file system
                       <br></br>
                PDFS Using <a href="https://github.com/fightinggg/pdfs-data-githubapi">Github Repo</a> To Stoage Files, Every File In Github Has Bean Encrypting
                <br></br>
                PDFS source code in <a href='https://github.com/fightinggg/pdfs'>here</a>
            </body>

            """;


    FsHandler fsHandler;
    Map<String, String> config;

    public HttpHandler(Map<String, String> config) {
        this.fsHandler = new FsHandler(config);
        this.config = config;
    }

    private boolean everyOneCanRead(String url, String method) {
        return method.equals("GET") && (
                url.equals("/") // 主页随便看
                        || url.startsWith("/fsapi/ls")// 公共文件随便看
                        || url.startsWith("/fsapi/webls") // 公共文件随便看
                        || url.startsWith("/fsapi/read") // 公共文件随便看
        ) && (
                !url.startsWith("/fsapi/ls/private")// 私人文件不许看
                        && !url.startsWith("/fsapi/webls/private")// 私人文件不许看
                        && !url.startsWith("/fsapi/read/private") // 私人文件不许看
        );
    }

    HttpRsp httpHandler(String url, String method, HttpHeaders headers, InputStream inputStream) throws IOException {
//        if (!everyOneCanRead(url, method)) {
//            boolean ok = false;
//            try {
//                CharSequence authorization = headers.get("Authorization");
//                String basic = authorization.toString();
//                String[] s1 = basic.split(" ");
//                String s = new String(Base64.decode(s1[1].getBytes(StandardCharsets.UTF_8)));
//                if (s.endsWith(config.get("key"))) {
//                    ok = true;
//                }
//            } catch (Exception ignored) {
//            }
//            if (!ok) {
//                String usage = "<html>Sorry , Your Need To Login. Click <a href='/'>Me</a> to Go Back</html>";
//                HttpRsp rsp = new HttpRsp(401, usage);
//                rsp.headers.put("WWW-Authenticate", "Basic realm=\"");
//                return rsp;
//            }
//        }


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
