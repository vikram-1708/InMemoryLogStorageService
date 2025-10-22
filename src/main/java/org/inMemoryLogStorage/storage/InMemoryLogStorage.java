package org.inMemoryLogStorage.storage;

import org.inMemoryLogStorage.models.LogEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe in-memory log storage engine.
 * <p>
 * Logs are indexed by:
 * - Service name
 * - Host ID
 * - Global timestamp (all logs across services/hosts)
 * <p>
 * Internally uses:
 * - ConcurrentHashMap for concurrent service/host partitions
 * - ConcurrentSkipListMap for timestamp ordering
 * - ConcurrentLinkedQueue for log buckets (append-only, efficient for write-heavy systems)
 */
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
     * <p>
     * Time Complexity: O(log n) (skiplist insert) + O(1) (queue append)
     * Space Complexity: O(1) extra per log (stored in 3 indexes: service, host, global)
     */
    public void addLog(LogEvent logEvent) {
        serviceIndex
                .computeIfAbsent(logEvent.getServiceName(), k -> new ConcurrentSkipListMap<>())
                .computeIfAbsent(logEvent.getTimestamp(), k -> new ConcurrentLinkedQueue<>())
                .add(logEvent);

        hostIndex
                .computeIfAbsent(logEvent.getHostId(), k -> new ConcurrentSkipListMap<>())
                .computeIfAbsent(logEvent.getTimestamp(), k -> new ConcurrentLinkedQueue<>())
                .add(logEvent);

        globalIndex
                .computeIfAbsent(logEvent.getTimestamp(), k -> new ConcurrentLinkedQueue<>())
                .add(logEvent);
    }

    /**
     * Retrieves logs for a given service within a time range.
     * <p>
     * Time Complexity: O(log n + k)
     * - O(log n): to locate start of range in skiplist
     * - O(k): to collect k logs in range
     * Space Complexity: O(k) for result list
     */
    public List<LogEvent> getLogsByService(String serviceName, long from, long to) {
        return queryIndex(serviceIndex.get(serviceName), from, to);
    }

    /**
     * Retrieves logs for a given host within a time range.
     * <p>
     * Time Complexity: O(log n + k)
     * - O(log n): to locate start of range
     * - O(k): to collect k logs in range
     * Space Complexity: O(k) for result list
     */
    public List<LogEvent> getLogsByHost(String hostId, long from, long to) {
        return queryIndex(hostIndex.get(hostId), from, to);
    }

    /**
     * Retrieves logs for a given service and host within a time range.
     * Optimized: directly queries host index, then filters by service.
     * <p>
     * Time Complexity: O(log n + k) + O(k) filtering = O(log n + k)
     * - O(log n): to locate start of range in host index
     * - O(k): to collect and filter k logs
     * Space Complexity: O(k) for result list
     */
    public List<LogEvent> getLogsByServiceAndHost(String serviceName, String hostId, long from, long to) {
        List<LogEvent> byHost = queryIndex(hostIndex.get(hostId), from, to);
        List<LogEvent> filtered = new ArrayList<>(byHost.size());
        for (LogEvent e : byHost) {
            if (e.getServiceName().equals(serviceName)) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    /**
     * Retrieves all logs across all services/hosts within a time range.
     * <p>
     * Time Complexity: O(log n + k)
     * - O(log n): to locate start of range in global index
     * - O(k): to collect k logs in range
     * Space Complexity: O(k) for result list
     */
    public List<LogEvent> getLogsGlobal(long from, long to) {
        return queryIndex(globalIndex, from, to);
    }

    /**
     * Internal helper to query logs from a timestamp-ordered index.
     * <p>
     * Time Complexity: O(log n + k)
     * - O(log n): to locate start of subMap
     * - O(k): iterate through logs in range
     * Space Complexity: O(k) for result list
     */
    private List<LogEvent> queryIndex(ConcurrentSkipListMap<Long, Queue<LogEvent>> index, long from, long to) {
        if (index == null) return Collections.emptyList();

        NavigableMap<Long, Queue<LogEvent>> range = index.subMap(from, true, to, true);

        List<LogEvent> result = new ArrayList<>();
        for (Queue<LogEvent> logs : range.values()) {
            result.addAll(logs);
        }
        return result;
    }
}
