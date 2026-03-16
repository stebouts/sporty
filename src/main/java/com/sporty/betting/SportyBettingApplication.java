package com.sporty.betting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SportyBettingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportyBettingApplication.class, args);
    }
}
