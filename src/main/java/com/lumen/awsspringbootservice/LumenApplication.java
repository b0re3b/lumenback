package com.lumen.awsspringbootservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LumenApplication {

    public static void main(String[] args) {
        SpringApplication.run(LumenApplication.class, args);
    }

}
