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

    public List<LogEvent> getLogsByService(String serviceName, long from, long to) {
        return inMemoryLogStorage.getLogsByService(serviceName, from, to);
    }

    public List<LogEvent> getLogsByServiceAndHost(String serviceName, String hostId, long from, long to) {
        return inMemoryLogStorage.getLogsByServiceAndHost(serviceName, hostId, from, to);
    }

    public List<LogEvent> getLogsByHost(String hostId, long from, long to) {
        return inMemoryLogStorage.getLogsByHost(hostId, from, to);
    }
}
