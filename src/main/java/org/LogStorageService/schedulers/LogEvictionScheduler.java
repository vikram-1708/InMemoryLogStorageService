package org.LogStorageService.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.LogStorageService.storage.InMemoryLogStorage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@SuppressWarnings("unused")
public class LogEvictionScheduler {

    private final InMemoryLogStorage logStorage;

    public LogEvictionScheduler(InMemoryLogStorage logStorage) {
        this.logStorage = logStorage;
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void evictOldLogs() {
        logStorage.evictOldLogs();
    }
}
