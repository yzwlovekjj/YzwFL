package com.yzw.FL.Common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fl")
@Data
public class FLConfig {
    //server/client
    private String type;
    //port
    private int port;
    //client number
    private int number;
}
