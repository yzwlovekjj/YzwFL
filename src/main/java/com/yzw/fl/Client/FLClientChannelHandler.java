package com.yzw.FL.Client;


import com.yzw.FL.Common.MySerializer;
import com.yzw.FL.Message.MessageType;
import com.yzw.FL.Message.Server_Model;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import java.io.*;


@Slf4j
public class FLClientChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg){
        //处理消息
        process(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }

    private void process(ByteBuf msg){
        short messageType = msg.readShort();
        if (messageType != MessageType.Client_Ready.getCode() && messageType != MessageType.Server_Model.getCode()) {
            throw new IllegalArgumentException("暂不支持此种数据" + messageType);
        }
        log.info("客户端接收数据成功,消息为" + MessageType.TypeFromCode(messageType));
        int length = msg.readInt();
        byte[] bytes = new byte[length];
        msg.readBytes(bytes);
        Object deserialize = MySerializer.deserialize(bytes, messageType);
        if (messageType == MessageType.Server_Model.getCode()) {
            processServerModel((Server_Model) deserialize);
        }
    }

    private void processServerModel(Server_Model msg){
        File file = new File("src/main/java/com/yzw/FL/Learning/model_state_dict_3.pth");
        // 文件夹不存在时尝试创建
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            log.info("Failed to create directory: " + file.getParentFile());
            return; // 目录创建失败，退出方法
        }
        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(msg.getParameters());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
