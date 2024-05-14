package com.yzw.FL.Server;


import com.yzw.FL.Common.Encode;
import com.yzw.FL.Common.FLConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FLServerInitializer extends ChannelInitializer<SocketChannel> {


    private FLConfig flConfig;

    @Override
    protected void initChannel(SocketChannel ch){
        //初始化pipeline
        ChannelPipeline pipeline = ch.pipeline();
        //
        pipeline.addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx){
                FLServer.channels.add(ctx.channel());
                FLServer.dataMap.put(ctx.channel(), false);
            }
        });
        //编码器
        pipeline.addLast(new Encode());
        //最大帧长度为Integer.MAX_VALUE; 长度字段偏移量:2; 长度字段长度:4; 长度字段为基准,距离内容字段:0;从 头剥离字节数:0
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,2,4,0,0));
        //处理器
        pipeline.addLast(new FLServerChannelHandler(flConfig));
    }

}
