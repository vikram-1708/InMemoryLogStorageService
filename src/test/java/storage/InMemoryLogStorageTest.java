package storage;

import org.LogStorageService.models.LogEvent;
import org.LogStorageService.storage.InMemoryLogStorage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryLogStorageTest {

    @Test
    public void testConcurrentReadsAndWrites() throws Exception {
        InMemoryLogStorage logStorage = new InMemoryLogStorage();
        ExecutorService executor = Executors.newFixedThreadPool(8);

        int writerThreads = 4;
        int readerThreads = 4;
        int logsPerWriter = 500;

        List<Callable<Void>> tasks = new ArrayList<>();

        // Writers
        for (int i = 0; i < writerThreads; i++) {
            tasks.add(() -> {
                for (int j = 0; j < logsPerWriter; j++) {
                    long ts = System.currentTimeMillis();
                    logStorage.addLog(new LogEvent(ts, "PaymentService", "payment-node-1", "log-" + j));
                }
                return null;
            });
        }

        // Readers
        for (int i = 0; i < readerThreads; i++) {
            tasks.add(() -> {
                for (int j = 0; j < 500; j++) {
                    long now = System.currentTimeMillis();
                    logStorage.getLogsByService("PaymentService", now - 1000, now);
                }
                return null;
            });
        }

        // Run all tasks together and fail if any throws
        List<Future<Void>> futures = executor.invokeAll(tasks);
        for (Future<Void> f : futures) {
            f.get(); // rethrows exceptions from workers
        }

        executor.shutdown();

        // Final log count check
        long now = System.currentTimeMillis();
        List<LogEvent> finalLogs = logStorage.getLogsByService("PaymentService", now - 60000, now);
        int expected = writerThreads * logsPerWriter;

        System.out.println("Final logs count: " + finalLogs.size());
        assertEquals(expected, finalLogs.size(), "Mismatch in final log count!");
    }

    @Test
    public void testEvictionRemovesOldLogs() {
        InMemoryLogStorage logStorage = new InMemoryLogStorage();

        long now = System.currentTimeMillis();

        // Insert an old log (older than retention)
        logStorage.addLog(new LogEvent(now - (2 * 60 * 60 * 1000), "PaymentService", "payment-node-1", "old-log"));

        // Insert a recent log (within retention)
        logStorage.addLog(new LogEvent(now, "PaymentService", "payment-node-1", "recent-log"));

        // Run eviction
        logStorage.evictOldLogs();

        List<LogEvent> logs = logStorage.getLogsByService("PaymentService", now - 24 * 60 * 60 * 1000, now);

        assertEquals(1, logs.size(), "Only recent log should remain after eviction");
        assertEquals("recent-log", logs.get(0).getLogMessage());
    }

    @Test
    public void testQueryByHost() {
        InMemoryLogStorage logStorage = new InMemoryLogStorage();
        long now = System.currentTimeMillis();

        logStorage.addLog(new LogEvent(now, "PaymentService", "payment-node-1", "host-log"));

        List<LogEvent> logs = logStorage.getLogsByHost("payment-node-1", now - 1000, now + 1000);

        assertEquals(1, logs.size());
        assertEquals("host-log", logs.get(0).getLogMessage());
    }

    @Test
    public void testQueryByServiceAndHostFiltersCorrectly() {
        InMemoryLogStorage logStorage = new InMemoryLogStorage();
        long now = System.currentTimeMillis();

        logStorage.addLog(new LogEvent(now, "PaymentService", "payment-node-1", "payment-log"));
        logStorage.addLog(new LogEvent(now, "OrderService", "payment-node-1", "order-log"));

        List<LogEvent> logs = logStorage.getLogsByServiceAndHost("PaymentService", "payment-node-1", now - 1000, now + 1000);

        assertEquals(1, logs.size());
        assertEquals("payment-log", logs.get(0).getLogMessage());
    }

    @Test
    public void testEmptyQueryReturnsEmptyList() {
        InMemoryLogStorage logStorage = new InMemoryLogStorage();
        long now = System.currentTimeMillis();

        List<LogEvent> logs = logStorage.getLogsByService("NonExistentService", now - 1000, now + 1000);

        assertNotNull(logs, "Query should not return null");
        assertTrue(logs.isEmpty(), "Expected empty list for non-existent service");
    }

    @Test
    public void testAddNullLogDoesNotThrow() {
        InMemoryLogStorage logStorage = new InMemoryLogStorage();
        assertDoesNotThrow(() -> logStorage.addLog(null), "Adding null log should not throw exception");
    }

    @Test
    public void testTimeRangeFiltering() {
        InMemoryLogStorage logStorage = new InMemoryLogStorage();

        long now = System.currentTimeMillis();
        long older = now - 5000;
        long newer = now + 5000;

        logStorage.addLog(new LogEvent(older, "PaymentService", "payment-node-1", "old-log"));
        logStorage.addLog(new LogEvent(now, "PaymentService", "payment-node-1", "current-log"));
        logStorage.addLog(new LogEvent(newer, "PaymentService", "payment-node-1", "future-log"));

        List<LogEvent> logs = logStorage.getLogsByService("PaymentService", now - 1000, now + 1000);

        assertEquals(1, logs.size(), "Only current-log should fall in the window");
        assertEquals("current-log", logs.get(0).getLogMessage());
    }

}
