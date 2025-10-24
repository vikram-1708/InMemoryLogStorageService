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
     * global timestamp index -> logs
     */
    private final ConcurrentSkipListMap<Long, Queue<LogEvent>> globalIndex = new ConcurrentSkipListMap<>();

    /**
     * Adds a log event into storage.
     * The log is indexed by service, host, and global timestamp.
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

            globalIndex.computeIfAbsent(logEvent.getTimestamp(), k -> new ConcurrentLinkedQueue<>())
                    .add(logEvent);

            log.debug("Added log: {}", logEvent);
        } catch (Exception e) {
            log.error("Failed to add logEvent: {}", logEvent, e);
            throw e;
        }
    }

    public List<LogEvent> getLogsByService(String serviceName, long from, long to) {
        log.debug("Querying logs by service={} from={} to={}", serviceName, from, to);
        return queryIndex(serviceIndex.get(serviceName), from, to);
    }

    public List<LogEvent> getLogsByHost(String hostId, long from, long to) {
        log.debug("Querying logs by host={} from={} to={}", hostId, from, to);
        return queryIndex(hostIndex.get(hostId), from, to);
    }

    public List<LogEvent> getLogsByServiceAndHost(String serviceName, String hostId, long from, long to) {
        log.debug("Querying logs by service={} host={} from={} to={}", serviceName, hostId, from, to);
        List<LogEvent> byHost = queryIndex(hostIndex.get(hostId), from, to);
        List<LogEvent> filtered = new ArrayList<>(byHost.size());
        for (LogEvent e : byHost) {
            if (e.getServiceName().equals(serviceName)) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    public List<LogEvent> getLogsGlobal(long from, long to) {
        log.debug("Querying global logs from={} to={}", from, to);
        return queryIndex(globalIndex, from, to);
    }

    private List<LogEvent> queryIndex(ConcurrentSkipListMap<Long, Queue<LogEvent>> index, long from, long to) {
        if (index == null || index.isEmpty()) {
            log.debug("No logs found for range {} - {}", from, to);
            return Collections.emptyList();
        }

        try {
            NavigableMap<Long, Queue<LogEvent>> range = index.subMap(from, true, to, true);
            List<LogEvent> result = new ArrayList<>();
            for (Queue<LogEvent> logs : range.values()) {
                result.addAll(logs);
            }
            return result;
        } catch (Exception e) {
            log.error("Error while querying logs from={} to={}", from, to, e);
            throw e;
        }
    }
}