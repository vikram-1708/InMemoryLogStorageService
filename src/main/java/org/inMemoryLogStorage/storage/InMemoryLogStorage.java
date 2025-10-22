package org.inMemoryLogStorage.storage;

import org.inMemoryLogStorage.models.LogEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class InMemoryLogStorage {
    // serviceName -> (timestamp -> List<LogEvent>)
    private final Map<String, ConcurrentSkipListMap<Long, List<LogEvent>>> serviceIndex = new ConcurrentHashMap<>();

    // hostId -> (timestamp -> List<LogEvent>)
    private final Map<String, ConcurrentSkipListMap<Long, List<LogEvent>>> hostIndex = new ConcurrentHashMap<>();

    public void addLog(LogEvent logEvent) {
        serviceIndex
                .computeIfAbsent(logEvent.getServiceName(), k -> new ConcurrentSkipListMap<>())
                .computeIfAbsent(logEvent.getTimestamp(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(logEvent);

        hostIndex
                .computeIfAbsent(logEvent.getHostId(), k -> new ConcurrentSkipListMap<>())
                .computeIfAbsent(logEvent.getTimestamp(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(logEvent);
    }

    public List<LogEvent> getLogsByService(String serviceName, long from, long to) {
        return queryIndex(serviceIndex.get(serviceName), from, to);
    }

    public List<LogEvent> getLogsByServiceAndHost(String serviceName, String hostId, long from, long to) {
        List<LogEvent> byService = queryIndex(serviceIndex.get(serviceName), from, to);
        List<LogEvent> filtered = new ArrayList<>();
        for (LogEvent e : byService) {
            if (e.getHostId().equals(hostId)) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    public List<LogEvent> getLogsByHost(String hostId, long from, long to) {
        return queryIndex(hostIndex.get(hostId), from, to);
    }

    private List<LogEvent> queryIndex(ConcurrentSkipListMap<Long, List<LogEvent>> index, long from, long to) {
        if (index == null) return Collections.emptyList();
        NavigableMap<Long, List<LogEvent>> range = index.subMap(from, true, to, true);
        List<LogEvent> result = new ArrayList<>();
        for (List<LogEvent> logs : range.values()) {
            result.addAll(logs);
        }
        return result;
    }
}