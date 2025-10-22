package org.inMemoryLogStorage.producers;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.inMemoryLogStorage.models.LogEvent;
import org.inMemoryLogStorage.storage.InMemoryLogStorage;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
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
            long currentTimeMillis = System.currentTimeMillis();
            String service = services[random.nextInt(services.length)];
            String host = hosts[random.nextInt(hosts.length)];
            String message = "Log message from " + service + "@" + host;

            LogEvent log = new LogEvent(currentTimeMillis, service, host, message);
            logStorage.addLog(log);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // allow clean shutdown
            }
        }
    }

    @PostConstruct
    public void startProducers() {
        int numProducers = 4; // could be externalized in application.properties
        executor = Executors.newFixedThreadPool(numProducers);
        for (int i = 0; i < numProducers; i++) {
            executor.submit(this);
        }
    }

    @PreDestroy
    public void stopProducers() {
        if (executor != null) {
            executor.shutdownNow(); // stop producers on shutdown
        }
    }
}
