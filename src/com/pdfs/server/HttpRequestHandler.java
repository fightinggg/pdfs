package com.pdfs.server;

import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@ChannelHandler.Sharable
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);
    HttpHandler httpHandler;

    public HttpRequestHandler(Map<String, String> config) {
        this.httpHandler = new HttpHandler(config);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest req) {
        if (Objects.equals(req.method().toString(), "GET") && Objects.equals(req.uri(), "/favicon.ico")) {
            ctx.writeAndFlush(Unpooled.buffer()).addListener(ChannelFutureListener.CLOSE);
            return;
        }


        ByteBufInputStream reqBody = new ByteBufInputStream(req.content());
        HttpRsp rsp = null;
        try {
            log.info("receive: {} {}", req.method(), req.uri());
            rsp = httpHandler.httpHandler(req.uri(), req.method().toString(), req.headers(), reqBody);
        } catch (IOException e) {
            log.error("", e);
            rsp = new HttpRsp(500, "Server Error");
        }

        rsp.headers.put("Server", "Pdfs");
        rsp.headers.put("Connection", "Keep-Alive");

        String httpLine = "HTTP/1.1 %d".formatted(rsp.status);
        String httpHeaders = rsp.headers.entrySet().stream().map(o -> o.getKey() + ": " + o.getValue()).collect(Collectors.joining("\n"));

        String msg = httpLine + "\n" + httpHeaders + "\n\n";
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ctx.writeAndFlush(Unpooled.buffer(bytes.length).writeBytes(bytes));

        InputStream body = rsp.body;
        readAndWrite(ctx, body);
    }


    void readAndWrite(ChannelHandlerContext ctx, InputStream inputStream) {
        int block = 1 << 10; // 1KB

        final boolean[] connect = {true};

        try {
            long totalSize = 0;
            while (connect[0]) {
                byte[] bytes = inputStream.readNBytes(block);
                if (bytes.length == 0) {
                    break;
                }
                if ((totalSize + bytes.length) / 1024 / 1024 > totalSize / 1024 / 1024) {
                    log.info("send bytes now={} MB", totalSize / (1024 * 1024.0));
                }
                totalSize += bytes.length;
                ctx.writeAndFlush(Unpooled.buffer(bytes.length).writeBytes(bytes)).addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        connect[0] = false;
                        Throwable throwable = future.cause();
                        if (throwable != null) {
                            log.error("send data failed... ", throwable);
                        }
                    }
                });
            }
            log.info("send bytes total={}", totalSize);
            ctx.writeAndFlush(Unpooled.buffer()).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            ctx.close();
            log.error("", e);
        }
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }
}