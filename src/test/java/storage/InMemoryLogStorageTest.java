package storage;

import org.LogStorageService.models.LogEvent;
import org.LogStorageService.storage.InMemoryLogStorage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
                    logStorage.addLog(new LogEvent(ts, "Service-1", "Host-1", "log-" + j));
                }
                return null;
            });
        }

        // Readers
        for (int i = 0; i < readerThreads; i++) {
            tasks.add(() -> {
                for (int j = 0; j < 500; j++) {
                    long now = System.currentTimeMillis();
                    logStorage.getLogsByService("Service-1", now - 1000, now);
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
        List<LogEvent> finalLogs = logStorage.getLogsByService("Service-1", now - 60000, now);
        int expected = writerThreads * logsPerWriter;

        System.out.println("Final logs count: " + finalLogs.size());
        assertEquals(expected, finalLogs.size(), "Mismatch in final log count!");
    }
}
