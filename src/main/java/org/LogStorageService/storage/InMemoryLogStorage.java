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

    private static final long RETENTION_MILLIS = 60 * 60 * 1000;

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
     *
     * While adding logs also we could do the logs eviction in below way also other than scheduler,
     * but to avoid higher computation to do it on every log write (considering it as high write throughput system)
     * we could maintain a 'lastEvictionTime' and based on that only we will do the eviction,
     * by this way we wont need a separate scheduler to do log eviction.
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
        List<LogEvent> logsByServiceAndHost = new ArrayList<>();
        for (LogEvent logEvent : logsByHost) {
            if (logEvent.getServiceName().equals(serviceName)) {
                logsByServiceAndHost.add(logEvent);
            }
        }
        return logsByServiceAndHost;
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

    public void evictOldLogs() {
        long cutoffTime = System.currentTimeMillis() - RETENTION_MILLIS;
        log.info("Evicting logs older than {}", cutoffTime);

        evictFromIndex(serviceIndex, cutoffTime);
        evictFromIndex(hostIndex, cutoffTime);
    }

    private void evictFromIndex(Map<String, ConcurrentSkipListMap<Long, Queue<LogEvent>>> index, long cutoffTime) {
        for (ConcurrentSkipListMap<Long, Queue<LogEvent>> map : index.values()) {
            try {
                NavigableMap<Long, Queue<LogEvent>> oldLogs = map.headMap(cutoffTime, false);
                oldLogs.clear();
            } catch (Exception e) {
                log.error("Error while evicting logs before {}", cutoffTime, e);
            }
        }
    }
}