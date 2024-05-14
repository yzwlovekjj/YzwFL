package com.yzw.FL.Client;

import com.yzw.FL.Message.Client_Ready;
import com.yzw.FL.Register.ServiceRegister;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.net.InetSocketAddress;

@Component
@Slf4j
@ConditionalOnProperty(name = "fl.type", havingValue = "client")
public class FLClient {
    //注册中心
    private final ServiceRegister serviceRegister;

    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;

    static{
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new FLClientInitializer());
    }

    @Autowired
    public FLClient(ServiceRegister serviceRegister) {
        this.serviceRegister = serviceRegister;
    }

    public void start() {
        //从注册中心发现FL服务端地址及其端口号
        InetSocketAddress address = serviceRegister.serviceDiscovery("FLServer");
        String server_host = address.getHostName();
        int server_port = address.getPort();
        try {
            ChannelFuture channelFuture = bootstrap.connect(server_host, server_port).sync();
            Channel channel = channelFuture.channel();
            ChannelFuture sendFuture = channel.writeAndFlush(new Client_Ready(System.currentTimeMillis()));
            sendFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("数据成功发送到服务器");
                } else {
                    log.error("发送数据失败", future.cause());
                }
            });
            channel.closeFuture().sync();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
