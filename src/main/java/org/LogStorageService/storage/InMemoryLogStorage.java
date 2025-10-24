package org.LogStorageService.storage;

import lombok.extern.slf4j.Slf4j;
import org.LogStorageService.models.LogEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;


@Slf4j
@Component
public class InMemoryLogStorage {

    /**
     * serviceName -> (timestamp -> logs)
     */
    private final Map<String, ConcurrentSkipListMap<Long, Queue<LogEvent>>> serviceIndex = new ConcurrentHashMap<>();

    /**
     * hostId -> (timestamp -> logs)
     */
    private final Map<String, ConcurrentSkipListMap<Long, Queue<LogEvent>>> hostIndex = new ConcurrentHashMap<>();

    /**
     * Adds a log event into storage.
     * The log is indexed by service, host.
     */
    public void addLog(LogEvent logEvent) {
        if (Objects.isNull(logEvent)) {
            log.debug("Attempted to add null logEvent - ignoring");
            return;
        }

        try {
            serviceIndex.computeIfAbsent(logEvent.getServiceName(), k -> new ConcurrentSkipListMap<>())
                    .computeIfAbsent(logEvent.getTimestamp(), k -> new ConcurrentLinkedQueue<>())
                    .add(logEvent);

            hostIndex.computeIfAbsent(logEvent.getHostId(), k -> new ConcurrentSkipListMap<>())
                    .computeIfAbsent(logEvent.getTimestamp(), k -> new ConcurrentLinkedQueue<>())
                    .add(logEvent);

            log.debug("Added log: {}", logEvent);
        } catch (Exception e) {
            log.error("Failed to add logEvent: {}", logEvent, e);
            throw e;
        }
    }

    public List<LogEvent> getLogsByService(String serviceName, long startTimeMillis, long endTimeMillis) {
        log.debug("Querying logs by service={} startTimeMillis={} endTimeMillis={}", serviceName, startTimeMillis, endTimeMillis);
        return queryIndex(serviceIndex.get(serviceName), startTimeMillis, endTimeMillis);
    }

    public List<LogEvent> getLogsByHost(String hostId, long startTimeMillis, long endTimeMillis) {
        log.debug("Querying logs by host={} startTimeMillis={} endTimeMillis={}", hostId, startTimeMillis, endTimeMillis);
        return queryIndex(hostIndex.get(hostId), startTimeMillis, endTimeMillis);
    }

    public List<LogEvent> getLogsByServiceAndHost(String serviceName, String hostId, long startTimeMillis, long endTimeMillis) {
        log.debug("Querying logs by service={} host={} startTimeMillis={} endTimeMillis={}", serviceName, hostId, startTimeMillis, endTimeMillis);
        List<LogEvent> logsByHost = queryIndex(hostIndex.get(hostId), startTimeMillis, endTimeMillis);
        List<LogEvent> filtered = new ArrayList<>(logsByHost.size());
        for (LogEvent logEvent : logsByHost) {
            if (logEvent.getServiceName().equals(serviceName)) {
                filtered.add(logEvent);
            }
        }
        return filtered;
    }

    private List<LogEvent> queryIndex(ConcurrentSkipListMap<Long, Queue<LogEvent>> index, long startTimeMillis, long endTimeMillis) {
        if (Objects.isNull(index) || index.isEmpty()) {
            log.debug("No logs found for range {} - {}", startTimeMillis, endTimeMillis);
            return Collections.emptyList();
        }

        try {
            NavigableMap<Long, Queue<LogEvent>> range = index.subMap(startTimeMillis, true, endTimeMillis, true);
            List<LogEvent> result = new ArrayList<>();
            for (Queue<LogEvent> logs : range.values()) {
                result.addAll(logs);
            }
            return result;
        } catch (Exception e) {
            log.error("Error while querying logs startTimeMillis={} endTimeMillis={}", startTimeMillis, endTimeMillis, e);
            throw e;
        }
    }
}