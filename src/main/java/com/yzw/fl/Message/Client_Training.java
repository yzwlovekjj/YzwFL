package com.yzw.FL.Message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Client_Training {

    private int epoch;

    private byte[] parameters;
}
