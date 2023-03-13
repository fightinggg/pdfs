package com.pdfs.server;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);
    HttpHandler httpHandler;

    public HttpRequestHandler(Map<String, String> config) {
        this.httpHandler = new HttpHandler(config);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest req) {


        ByteBufInputStream reqBody = new ByteBufInputStream(req.content());
        HttpRsp rsp = null;
        try {
            rsp = httpHandler.httpHandler(req.uri(), req.method().toString(), reqBody);
        } catch (IOException e) {
            log.error("", e);
            rsp = new HttpRsp(500, "Server Error");
        }

        rsp.headers.put("Server", "Pdfs");
        rsp.headers.put("Connection", "Keep-Alive");

        String httpLine = "HTTP/1.1 %d".formatted(rsp.status);
        String httpHeaders = rsp.headers.entrySet().stream().map(o -> o.getKey() + ": " + o.getValue()).collect(Collectors.joining("\n"));

        String msg = httpLine + "\n" + httpHeaders + "\n\n";
        ctx.writeAndFlush(Unpooled.buffer().writeBytes(msg.getBytes(StandardCharsets.UTF_8)));

        InputStream body = rsp.body;
        int block = 1 << 20; // 1MB
        byte[] bytes = new byte[block];

        try {
            while (true) {
                int size = body.read(bytes);
                if (size == -1) {
                    break;
                }
                ctx.writeAndFlush(Unpooled.buffer().writeBytes(bytes, 0, size));
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            ctx.flush();
            ctx.close().addListener(ChannelFutureListener.CLOSE);
        }
    }
}