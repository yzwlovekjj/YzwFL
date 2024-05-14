package com.yzw.FL.Common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yzw.FL.Message.MessageType;


public class MySerializer {

    public static byte[] serialize(Object object) {
        return JSONObject.toJSONBytes(object);
    }

    public static Object deserialize(byte[] bytes, short messageType) {
        return JSON.parseObject(bytes, MessageType.ClassFromCode(messageType));
    }
}
