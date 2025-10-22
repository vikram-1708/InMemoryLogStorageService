package org.inMemoryLogStorage.service;

import org.inMemoryLogStorage.models.LogEvent;
import org.inMemoryLogStorage.storage.InMemoryLogStorage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogQueryService {

    private final InMemoryLogStorage logStorage;

    public LogQueryService(InMemoryLogStorage logStorage) {
        this.logStorage = logStorage;
    }

    public List<LogEvent> getLogsByService(String service, long from, long to) {
        return logStorage.getLogsByService(service, from, to);
    }

    public List<LogEvent> getLogsByServiceAndHost(String service, String host, long from, long to) {
        return logStorage.getLogsByServiceAndHost(service, host, from, to);
    }

    public List<LogEvent> getLogsByHost(String host, long from, long to) {
        return logStorage.getLogsByHost(host, from, to);
    }
}
