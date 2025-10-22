package org.inMemoryLogStorage.producers;

import jakarta.annotation.PostConstruct;
import org.inMemoryLogStorage.models.LogEvent;
import org.inMemoryLogStorage.storage.InMemoryLogStorage;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@SuppressWarnings("unused")
public class LogProducer implements Runnable {

    private final InMemoryLogStorage logStorage;
    private final Random random = new Random();
    private final String[] services = {"PaymentService", "OrderService", "InventoryService"};
    private final String[] hosts = {"host1", "host2", "host3", "host4"};

    // Spring injects InMemoryLogStorage bean (from LogStorageConfig)
    public LogProducer(InMemoryLogStorage logStorage) {
        this.logStorage = logStorage;
    }

    @Override
    public void run() {
        while (true) {
            long timestamp = System.currentTimeMillis();
            String service = services[random.nextInt(services.length)];
            String host = hosts[random.nextInt(hosts.length)];
            String message = "Log message from " + service + "@" + host;

            LogEvent log = new LogEvent(timestamp, service, host, message);
            logStorage.addLog(log);

            try {
                Thread.sleep(500); // produce every 0.5 sec
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // Start producer thread after Spring context is initialized
    @PostConstruct
    public void startProducerThread() {
        Thread producerThread = new Thread(this, "LogProducer-Thread");
        producerThread.setDaemon(true);
        producerThread.start();
    }
}
