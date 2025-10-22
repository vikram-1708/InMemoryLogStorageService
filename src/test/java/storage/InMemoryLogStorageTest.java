package storage;

import org.LogStorageService.models.LogEvent;
import org.LogStorageService.storage.InMemoryLogStorage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class InMemoryLogStorageTest {

    @Test
    public void testConcurrentReadsAndWrites() throws InterruptedException {
        InMemoryLogStorage logStorage = new InMemoryLogStorage();
        ExecutorService executor = Executors.newFixedThreadPool(8);

        int writerThreads = 4;
        int readerThreads = 4;
        CountDownLatch latch = new CountDownLatch(writerThreads + readerThreads);

        // Writers
        for (int i = 0; i < writerThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 500; j++) {
                        long ts = System.currentTimeMillis();
                        logStorage.addLog(new LogEvent(ts, "Service-1", "Host-1", "log-" + j));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Readers
        for (int i = 0; i < readerThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 500; j++) {
                        long now = System.currentTimeMillis();
                        List<LogEvent> logs = logStorage.getLogsByService("Service-1", now - 1000, now);
                        if (!logs.isEmpty()) {
                            assertFalse(logs.contains(null), "Null log found!");
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Final check
        long now = System.currentTimeMillis();
        List<LogEvent> finalLogs = logStorage.getLogsByService("Service-1", now - 60000, now);
        System.out.println("Final logs count: " + finalLogs.size());

        assertFalse(finalLogs.isEmpty(), "Expected logs but found none!");
    }
}
