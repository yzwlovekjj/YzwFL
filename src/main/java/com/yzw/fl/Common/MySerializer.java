package com.yzw.FL.Common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yzw.FL.Message.MessageType;


public class MySerializer {

    public static byte[] serialize(Object object) {
        return JSONObject.toJSONBytes(object);
    }

    public static Object deserialize(byte[] bytes, short messageType){
        try {
            return JSON.parseObject(bytes, MessageType.ClassFromCode(messageType));
        } catch (JSONException e) {
            throw new IllegalArgumentException("反序列化失败，数据格式有误", e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("反序列化失败，类型转换错误", e);
        }
    }

}
