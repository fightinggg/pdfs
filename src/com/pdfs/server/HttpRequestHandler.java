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
        int block = 1 << 10; // 1KB

        final boolean[] connect = {true};


        try {
            long totalSize = 0;
            while (connect[0]) {
                ByteBuf buffer = Unpooled.buffer(block);
                int size = buffer.writeBytes(body, block);
                if (size <= 0) {
                    break;
                }
                totalSize += size;
                ctx.writeAndFlush(buffer).addListener((ChannelFutureListener) future -> {
                    if (future.state().equals(Future.State.FAILED)) {
                        log.error("failed: {}", future);
                        connect[0] = false;
                    }
                });
            }
            log.info("send connect={}, bytes={}", connect[0], totalSize);
        } catch (Exception e) {
            log.error("", e);
        } finally {
            ctx.writeAndFlush(Unpooled.buffer()).addListener(ChannelFutureListener.CLOSE);
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