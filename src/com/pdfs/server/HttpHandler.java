package com.pdfs.server;

import com.pdfs.fs.Factory;
import com.pdfs.normalfs.DirectFileNormalFsImpl;
import com.pdfs.normalfs.NormalFs;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class HttpHandler {


    FsHandler fsHandler = new FsHandler();

    int httpHandler(String url, String method, InputStream inputStream, OutputStream outputStream) throws IOException {
        if (url.contains("//")) {
            IOUtils.write("Bad URL: " + url, outputStream);
            return 400;
        }
        if (method.equals("GET") && url.startsWith("/static/")) {
            return getStaticFiles(url, outputStream);
        } else if (url.startsWith("/fsapi/")) {
            return fsHandler.httpHandler(url, method, inputStream, outputStream);
        } else {
            IOUtils.write("Welcome To PDFS!", outputStream);
            return 200;
        }
    }

    private int getStaticFiles(String url, OutputStream outputStream) throws IOException {
        if (url.contains("..")) {
            IOUtils.write("url could not contain '..' ", outputStream);
            return 403;
        } else {
            try {
                IOUtils.copy(new FileInputStream(url), outputStream);
                return 200;
            } catch (Exception e) {
                IOUtils.write("could not found " + url, outputStream);
                return 404;
            }
        }
    }


}
