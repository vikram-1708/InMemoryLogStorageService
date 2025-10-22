package org.inMemoryLogStorage;

import org.inMemoryLogStorage.producers.LogProducer;
import org.inMemoryLogStorage.storage.InMemoryLogStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LogStorageApplication {
    public static void main(String[] args) {
        InMemoryLogStorage logStore = new InMemoryLogStorage();

        // Start producer thread to simulate logs
        Thread producerThread = new Thread(new LogProducer(logStore));
        producerThread.setDaemon(true);
        producerThread.start();

        // Pass logStore as a bean into Spring context
        SpringApplication app = new SpringApplication(LogStorageApplication.class);
        app.addInitializers(ctx -> ctx.getBeanFactory().registerSingleton("inMemoryLogStore", logStore));
        app.run(args);
    }
}