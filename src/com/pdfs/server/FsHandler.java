package com.pdfs.server;

import com.pdfs.fsfactory.Factory;
import com.pdfs.normalfs.NormalFs;
import com.pdfs.server.fshander.FsReadHandler;
import com.pdfs.server.fshander.FsWebLsHandler;
import com.pdfs.server.fshander.FsWriteBigHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class FsHandler {

    NormalFs fs;

    FsWriteBigHandler fsWriteBigHandler;
    FsReadHandler fsReadHandler;
    FsWebLsHandler fsWebLsHandler;

    public FsHandler(Map<String, String> config) {
        fs = Factory.getNormalFs(config);

        fsWriteBigHandler = new FsWriteBigHandler(fs);
        fsReadHandler = new FsReadHandler(fs);
        fsWebLsHandler = new FsWebLsHandler(fs);
    }

    HttpRsp httpHandler(String url, String method, InputStream inputStream) throws IOException {
        if (method.equals("GET") && url.startsWith("/fsapi/read/")) {
            return fsReadHandler.readHandler(url, method, inputStream);
        }
        if (method.equals("POST") && url.startsWith("/fsapi/write/")) {
            return writeHandler(url, method, inputStream);
        }
        if (method.equals("GET") && url.startsWith("/fsapi/ls/")) {
            return lsHandler(url, method, inputStream);
        }
        if (method.equals("GET") && url.startsWith("/fsapi/webls/")) {
            return fsWebLsHandler.webLsHandler(url, method, inputStream);
        }
        if (method.equals("POST") && url.startsWith("/fsapi/writeBig/")) {
            return fsWriteBigHandler.writeBigHandler(url, method, inputStream);
        }
        return new HttpRsp(403, "SORRY! Your Request Is Not Valid!");
    }


    HttpRsp writeHandler(String url, String method, InputStream inputStream) throws IOException {
//        try {
//            String path = url.substring("/fsapi/write/".length() - 1);
//            fs.write(path, 0, 0, inputStream);
//            return 200;
//        } catch (Exception e) {
//            outputStream.write("Server ERROR ".getBytes(StandardCharsets.UTF_8));
//            log.error("", e);
//            return 500;
//        }
        return new HttpRsp(500, "");

    }

    HttpRsp lsHandler(String url, String method, InputStream inputStream) throws IOException {
        return new HttpRsp(500, "");
    }


}
