package com.pdfs.server.fshander;

import com.pdfs.normalfs.NormalFs;
import com.pdfs.server.HttpRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FsWriteBigHandler {
    Logger log = LoggerFactory.getLogger(FsReadHandler.class);

    NormalFs fs;


    public FsWriteBigHandler(NormalFs fs) {
        this.fs = fs;
    }

    public HttpRsp writeBigHandler(String url, String method, InputStream inputStream) throws IOException {
        String path = url.substring("/fsapi/writeBig/".length());
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);


        String[] split = path.split("/");
        long total, current;
        try {
            total = Long.parseLong(split[0]);
            current = Long.parseLong(split[1]) << 20;
        } catch (Exception e) {
            return new HttpRsp(400, "BAD REQUEST");
        }
        path = "/" + String.join("/", Arrays.stream(split).toList().subList(2, split.length));
        try {
            fs.write(path, total, current, inputStream);
        } catch (Exception e) {
            log.error("", e);
            return new HttpRsp(500, "Server ERROR ");
        }
        return new HttpRsp(200, "");

    }


}
