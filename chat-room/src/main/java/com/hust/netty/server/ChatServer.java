package com.hust.netty.server;

import com.hust.netty.server.handler.ImIdleHandler;
import com.hust.netty.server.handler.WebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatServer {
    private final static int PORT = 8002;

    public static void main(String[] args) {
        try {
            new ChatServer().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        Thread.currentThread().setName("chat-server");
        //boss线程
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //worker线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //netty服务,启动引擎
            ServerBootstrap b = new ServerBootstrap();
            //主从模式
            b.group(bossGroup, workerGroup)
            //主线程处理类
            .channel(NioServerSocketChannel.class)
            //配置信息
            .option(ChannelOption.SO_BACKLOG, 1024)// 针对主线程配置
//          .childOption(ChannelOption.SO_KEEPALIVE,true);//子线程配置
            //子线程的处理，Handler
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast("idle", new ImIdleHandler(9 * 60,  9 * 60, 9 * 60)); // 心跳检测，9分钟检测一次，比nginx设置的10分钟短一点
                    pipeline.addLast("http-codec", new HttpServerCodec()); // Http消息编码解码
                    pipeline.addLast("aggregator", new HttpObjectAggregator(65536)); // Http消息组装
                    pipeline.addLast("http-chunked", new ChunkedWriteHandler()); // WebSocket通信支持
                    //http请求的业务逻辑处理
                    pipeline.addLast("handler", new WebSocketHandler()); // WebSocket服务端Handler
                }
            });

            //等待客户端连接
            ChannelFuture f = b.bind(PORT).sync();
            log.info("chat-server running in port " + PORT);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

}
