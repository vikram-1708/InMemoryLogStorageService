package org.inMemoryLogStorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class LogStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogStorageApplication.class, args);
    }
}