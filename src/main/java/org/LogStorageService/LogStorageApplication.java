package org.LogStorageService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogStorageApplication.class, args);
    }
}