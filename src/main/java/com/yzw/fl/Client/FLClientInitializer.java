package com.yzw.FL.Client;


import com.yzw.FL.Common.Encode;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


public class FLClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch){
        ChannelPipeline pipeline = ch.pipeline();
        //编码器
        pipeline.addLast(new Encode());
        //最大帧长度为Integer.MAX_VALUE;
        //长度字段偏移量:2
        //长度字段长度:4
        //长度字段为基准,距离内容字段:0
        //从头剥离字节数:0
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,2,4,0,0));
        //处理器
        pipeline.addLast(new FLClientChannelHandler());


    }

}
