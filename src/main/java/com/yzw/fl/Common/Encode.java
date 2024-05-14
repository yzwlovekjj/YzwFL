package com.yzw.FL.Common;

import com.yzw.FL.Message.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.NoArgsConstructor;


@NoArgsConstructor
public class Encode extends MessageToByteEncoder<Object> {

    /* Protocol
            * +--------+-------------------------------+
            * | MessageType | ContentLength | Content |
            * |    2bytes   |     4bytes    |   ----  |
            * +--------+-------------------------------+
    */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf){
        //写入数据类型
        byteBuf.writeShort(MessageType.typeFromObject(o));
        //写入序列化数组长度
        byte[] serialize = MySerializer.serialize(o);
        byteBuf.writeInt(serialize.length);
        //写入序列化字节数组
        byteBuf.writeBytes(serialize);
    }
}
