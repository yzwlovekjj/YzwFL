package com.yzw.FL.Message;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageType {
    Client_Ready((short) 0, Client_Ready.class),
    Server_Model((short) 1, Server_Model.class),
    Client_Training((short) 2, Client_Training.class),
    Client_Over((short) 3, Client_Over.class);

    private final short code;
    private final Class<?> type;

    public short getCode() {
        return code;
    }

    public Class<?> getType() {
        return type;
    }

    public static Class<?> ClassFromCode(int code) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.getCode() == code) {
                return messageType.getType();
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + code + "]");
    }

    public static MessageType TypeFromCode(int code) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.getCode() == code) {
                return messageType;
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + code + "]");
    }

    public static Short typeFromObject(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("The object cannot be null.");
        }
        for (MessageType messageType : MessageType.values()) {
            if (o.getClass().equals(messageType.getType())) {
                return messageType.getCode();
            }
        }
        throw new IllegalArgumentException("No matching MessageType for class: " + o.getClass().getName());
    }
}