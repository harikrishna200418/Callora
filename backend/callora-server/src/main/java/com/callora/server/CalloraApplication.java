package com.callora.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class CalloraApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalloraApplication.class, args);
    }
}
