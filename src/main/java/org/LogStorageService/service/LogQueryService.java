package org.LogStorageService.service;

import org.LogStorageService.models.LogEvent;
import org.LogStorageService.storage.InMemoryLogStorage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogQueryService {

    private final InMemoryLogStorage inMemoryLogStorage;

    public LogQueryService(InMemoryLogStorage inMemoryLogStorage) {
        this.inMemoryLogStorage = inMemoryLogStorage;
    }

    public List<LogEvent> getLogsByService(String serviceName, long startTimeMillis, long endTimeMillis) {
        return inMemoryLogStorage.getLogsByService(serviceName, startTimeMillis, endTimeMillis);
    }

    public List<LogEvent> getLogsByServiceAndHost(String serviceName, String hostId, long startTimeMillis, long endTimeMillis) {
        return inMemoryLogStorage.getLogsByServiceAndHost(serviceName, hostId, startTimeMillis, endTimeMillis);
    }

    public List<LogEvent> getLogsByHost(String hostId, long startTimeMillis, long endTimeMillis) {
        return inMemoryLogStorage.getLogsByHost(hostId, startTimeMillis, endTimeMillis);
    }
}
