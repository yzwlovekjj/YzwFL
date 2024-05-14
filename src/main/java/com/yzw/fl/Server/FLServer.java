package com.yzw.FL.Server;

import com.yzw.FL.Common.FLConfig;
import com.yzw.FL.Register.ServiceRegister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ConditionalOnProperty(name = "fl.type", havingValue = "server")
public class FLServer {

    //训练轮次
    public static int epoch = 1;

    //管理连接
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    //连接<->训练轮数
    public static Map<Channel, Boolean> dataMap = new ConcurrentHashMap<>();

    //注册中心
    private final ServiceRegister serviceRegister;

    //配置文件
    private final FLConfig flconfig;

    @Autowired
    public FLServer(ServiceRegister serviceRegister, FLConfig flconfig) {
        this.serviceRegister = serviceRegister;
        this.flconfig = flconfig;
    }

    public void start() {
        if (!register()) {
            log.info("联邦学习服务端启动失败");
            return;
        }
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup(10);
        try {
            // 启动netty服务器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 初始化
            serverBootstrap.group(bossGroup,workGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new FLServerInitializer(flconfig));
            // 同步阻塞
            ChannelFuture channelFuture = serverBootstrap.bind(flconfig.getPort()).sync();
            log.info("联邦学习服务端启动了...");
            // 死循环监听
            channelFuture.channel().closeFuture().sync();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

    private boolean register() {
        //本机的 IP 地址
        String ipAddress;
        try {
            // 获取本机的 InetAddress 实例
            InetAddress localhost = InetAddress.getLocalHost();
            // 获取本机的 IP 地址
             ipAddress = localhost.getHostAddress();
        } catch (UnknownHostException e) {
            log.info("无法获取本机的 IP 地址：" + e.getMessage());
            return false;
        }
        serviceRegister.register("FLServer", ipAddress, flconfig.getPort());
        return true;
    }


}
