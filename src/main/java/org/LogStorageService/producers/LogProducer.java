package org.LogStorageService.producers;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.LogStorageService.models.LogEvent;
import org.LogStorageService.storage.InMemoryLogStorage;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@SuppressWarnings("unused")
public class LogProducer {

    private final InMemoryLogStorage logStorage;
    private final Random random = new Random();
    private ScheduledExecutorService executor;

    private static final Map<String, String[]> SERVICE_HOSTS = Map.of(
            "PaymentService", new String[]{"payment-node-1", "payment-node-2", "payment-node-3"},
            "OrderService", new String[]{"order-node-1", "order-node-2"},
            "InventoryService", new String[]{"inventory-node-1", "inventory-node-2", "inventory-node-3"}
    );

    private static final List<String> SERVICES = new ArrayList<>(SERVICE_HOSTS.keySet());

    public LogProducer(InMemoryLogStorage logStorage) {
        this.logStorage = logStorage;
    }

    private void produceLog() {
        try {
            long currentTimeMillis = System.currentTimeMillis();

            String service = SERVICES.get(random.nextInt(SERVICES.size()));

            String[] hosts = SERVICE_HOSTS.get(service);
            String host = hosts[random.nextInt(hosts.length)];

            String logMessage = "Log message from " + service + "@" + host;
            LogEvent logEvent = new LogEvent(currentTimeMillis, service, host, logMessage);

            log.debug("Producing log: {}", logEvent);
            logStorage.addLog(logEvent);
        } catch (Exception e) {
            log.error("Error while producing log", e);
        }
    }

    @PostConstruct
    public void startProducers() {
        int logProducerThreads = 4;
        log.info("Starting {} LogProducer tasks", logProducerThreads);
        executor = Executors.newScheduledThreadPool(logProducerThreads);
        for (int i = 0; i < logProducerThreads; i++) {
            executor.scheduleAtFixedRate(this::produceLog, 0, 500, TimeUnit.MILLISECONDS);
        }
    }

    @PreDestroy
    public void stopProducers() {
        if (executor != null) {
            log.info("Stopping LogProducer tasks...");
            executor.shutdownNow();
        }
    }
}
