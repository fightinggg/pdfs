package com.pdfs.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderUtil.is100ContinueExpected;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    HttpHandler httpHandler = new HttpHandler();


    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufOutputStream byteBufOutputStream = new ByteBufOutputStream(buffer);


        ByteBufInputStream byteBufInputStream = new ByteBufInputStream(req.content());
        int status = httpHandler.httpHandler(req.uri(), req.method().toString(), byteBufInputStream, byteBufOutputStream);

        // 创建http响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(status),
                buffer);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}