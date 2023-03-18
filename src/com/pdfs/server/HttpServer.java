package com.pdfs.server;

import com.pdfs.server.fshander.SecureSocketSslContextFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.OpenSslEngine;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.util.Map;

public class HttpServer {

    Logger log = LoggerFactory.getLogger(HttpServer.class);

    Map<String, String> config;

    public HttpServer(Map<String, String> config) {
        this.config = config;
    }

    public void start() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();
        bootstrap.group(boss, work)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
//                        SSLEngine sslEngine = SecureSocketSslContextFactory.getServerContext().createSSLEngine();
//                        sslEngine.setUseClientMode(false);
//                        pipeline.addLast(new SslHandler(sslEngine));
                        pipeline.addLast(new HttpRequestDecoder());// http 编解码
                        pipeline.addLast("httpAggregator", new HttpObjectAggregator(2 * 1024 * 1024)); // 2MB                                                                    512*1024为接收的最大contentlength
                        pipeline.addLast(new HttpRequestHandler(config));// 请求处理器
                    }
                });

        int port = Integer.parseInt(config.get("port"));

        ChannelFuture f = bootstrap.bind(new InetSocketAddress(port)).sync();
        log.info("pdfs server listen on port : {}", port);
        f.channel().closeFuture().sync();

    }

}
