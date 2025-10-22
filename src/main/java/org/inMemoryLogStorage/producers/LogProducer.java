package org.inMemoryLogStorage.producers;

import org.inMemoryLogStorage.models.LogEvent;
import org.inMemoryLogStorage.storage.InMemoryLogStorage;

import java.util.Random;

public class LogProducer implements Runnable {
    private final InMemoryLogStorage logStorage;
    private final Random random = new Random();
    private final String[] services = {"service1", "service2", "service3"};
    private final String[] hosts = {"host1", "host2"};

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
}
