package com.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"co.kr.dains.crowd.estimation.common", "com"})
public class EventGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventGeneratorApplication.class, args);
    }

}
