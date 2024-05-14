package com.yzw.FL.Message;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Server_Model {

    private int epoch;

    private byte[] parameters;

}
