package com.pdfs.server;

import com.pdfs.utils.IOUtils;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.function.Supplier;
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

        HttpRsp finalRsp = rsp;
        InputStream merge = IOUtils.merge(List.of(() -> new ByteArrayInputStream(bytes), () -> finalRsp.body));
        readAndWrite(ctx, merge, 0L);
    }


    void readAndWrite(ChannelHandlerContext ctx, InputStream inputStream, long totalSize) {
        try {
            int block = 1 << 10; // 1KB

            byte[] bytes = inputStream.readNBytes(block);
            int length = bytes.length;
            if (length == 0) {
                log.info("send bytes total={}", totalSize);
                ctx.close();
                return;
            }


            if ((totalSize + length) / 1024 / 1024 > totalSize / 1024 / 1024) {
                log.info("send bytes now={} MB", totalSize / (1024 * 1024.0));
            }


            ctx.writeAndFlush(Unpooled.buffer(length).writeBytes(bytes)).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    Throwable throwable = future.cause();
                    if (throwable != null) {
                        log.error("send data failed... ", throwable);
                    }
                    future.channel().close();
                } else {
                    readAndWrite(ctx, inputStream, totalSize + length);
                }
            });


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