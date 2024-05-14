package com.yzw.FL;

import com.yzw.FL.Client.FLClient;
import com.yzw.FL.Common.FLConfig;
import com.yzw.FL.Server.FLServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FLApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(FLApplication.class, args);
        FLConfig flConfig = context.getBean(FLConfig.class);
        if ("server".equals(flConfig.getType())) {
            FLServer server = context.getBean(FLServer.class);
            server.start();
        } else if ("client".equals(flConfig.getType())) {
            FLClient client = context.getBean(FLClient.class);
            client.start();
        } else {
            System.out.println("Unknown type: " + flConfig.getType());
        }
    }
}
