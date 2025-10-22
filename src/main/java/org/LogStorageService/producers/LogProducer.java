package org.LogStorageService.producers;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.LogStorageService.models.LogEvent;
import org.LogStorageService.storage.InMemoryLogStorage;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
@SuppressWarnings("unused")
public class LogProducer implements Runnable {

    private final InMemoryLogStorage logStorage;
    private final Random random = new Random();
    private ExecutorService executor;

    private final String[] services = {"PaymentService", "OrderService", "InventoryService"};
    private final String[] hosts = {"host1", "host2", "host3", "host4"};

    public LogProducer(InMemoryLogStorage logStorage) {
        this.logStorage = logStorage;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                long currentTimeMillis = System.currentTimeMillis();
                String service = services[random.nextInt(services.length)];
                String host = hosts[random.nextInt(hosts.length)];
                String message = "Log message from " + service + "@" + host;

                LogEvent logEvent = new LogEvent(currentTimeMillis, service, host, message);

                log.debug("Producing log: {}", logEvent);
                logStorage.addLog(logEvent);

                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.info("Producer thread interrupted, shutting down...");
                Thread.currentThread().interrupt(); // graceful shutdown
            } catch (Exception e) {
                log.error("Unexpected error in LogProducer thread", e);
            }
        }
    }

    @PostConstruct
    public void startProducers() {
        int numProducers = 4; // could be externalized via application.properties
        log.info("Starting {} LogProducer threads", numProducers);
        executor = Executors.newFixedThreadPool(numProducers);
        for (int i = 0; i < numProducers; i++) {
            executor.submit(this);
        }
    }

    @PreDestroy
    public void stopProducers() {
        if (executor != null) {
            log.info("Stopping LogProducer threads...");
            executor.shutdownNow();
        }
    }
}
