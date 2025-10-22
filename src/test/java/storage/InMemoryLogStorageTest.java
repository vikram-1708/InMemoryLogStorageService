package InMemoryLogStorageConcurrencyTest;

import org.inMemoryLogStorage.models.LogEvent;
import org.inMemoryLogStorage.storage.InMemoryLogStorage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class InMemoryLogStorageConcurrencyTest {
    @Test
    public void testConcurrentReadsAndWrites() throws InterruptedException {
        InMemoryLogStorage logStorage = new InMemoryLogStorage();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Random random = new Random();

        int writerThreads = 5;
        int readerThreads = 5;
        int totalTasks = writerThreads + readerThreads;

        CountDownLatch latch = new CountDownLatch(totalTasks);

        // Writers: add logs continuously
        for (int i = 0; i < writerThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 1000; j++) {
                        long ts = System.currentTimeMillis();
                        String service = "Service-" + (random.nextInt(3) + 1);
                        String host = "Host-" + (random.nextInt(3) + 1);
                        String msg = "Log from " + service + "@" + host;

                        logStorage.addLog(new LogEvent(ts, service, host, msg));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Readers: query logs continuously
        for (int i = 0; i < readerThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 1000; j++) {
                        long now = System.currentTimeMillis();
                        List<LogEvent> logs = logStorage.getLogsByService("Service-1", now - 10000, now);
                        // Just touch the list to simulate read
                        if (!logs.isEmpty()) {
                            assertFalse(logs.contains(null), "Null log found!");
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // wait for all tasks
        executor.shutdown();

        // Final validation
        long now = System.currentTimeMillis();
        List<LogEvent> finalLogs = logStorage.getLogsByService("Service-1", now - 60000, now);
        System.out.println("Final logs count: " + finalLogs.size());

        // Make sure some logs actually got written
        assertFalse(finalLogs.isEmpty(), "Expected logs but found none!");
    }
}
