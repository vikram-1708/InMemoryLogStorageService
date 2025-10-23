package org.LogStorageService.controller;

import lombok.extern.slf4j.Slf4j;
import org.LogStorageService.models.LogEvent;
import org.LogStorageService.service.LogQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
@Slf4j
@SuppressWarnings("unused")
public class LogController {

    private final LogQueryService logQueryService;

    public LogController(LogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @RequestMapping("/service/{serviceName}")
    public List<LogEvent> getByService(@PathVariable String serviceName,
                                       @RequestParam long from,
                                       @RequestParam long to) {
        validateTimeRange(from, to);
        log.info("Fetching logs for service={} from={} to={}", serviceName, from, to);
        List<LogEvent> logs = logQueryService.getLogsByService(serviceName, from, to);
        log.debug("Found {} logs for service={}", logs.size(), serviceName);
        return logs;
    }

    @GetMapping("/service/{serviceName}/host/{hostId}")
    public List<LogEvent> getByServiceAndHost(@PathVariable String serviceName,
                                              @PathVariable String hostId,
                                              @RequestParam long from,
                                              @RequestParam long to) {
        validateTimeRange(from, to);
        log.info("Fetching logs for service={} host={} from={} to={}", serviceName, hostId, from, to);
        List<LogEvent> logs = logQueryService.getLogsByServiceAndHost(serviceName, hostId, from, to);
        log.debug("Found {} logs for service={} host={}", logs.size(), serviceName, hostId);
        return logs;
    }

    @GetMapping("/host/{hostId}")
    public List<LogEvent> getByHost(@PathVariable String hostId,
                                    @RequestParam long from,
                                    @RequestParam long to) {
        validateTimeRange(from, to);
        log.info("Fetching logs for host={} from={} to={}", hostId, from, to);
        List<LogEvent> logs = logQueryService.getLogsByHost(hostId, from, to);
        log.debug("Found {} logs for host={}", logs.size(), hostId);
        return logs;
    }

    /**
     * Helper to validate that "from" <= "to".
     * Throws IllegalArgumentException if invalid.
     */
    private void validateTimeRange(long from, long to) {
        if (from > to) {
            log.warn("Invalid time range: from {} > to={}", from, to);
            throw new IllegalArgumentException("Invalid time range: 'from' must be <= 'to'");
        }
    }
}
