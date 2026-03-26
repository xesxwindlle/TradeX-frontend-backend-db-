package com.tradex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling       //enable @Scheduled
public class TradeXApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradeXApplication.class, args);
    }
}
