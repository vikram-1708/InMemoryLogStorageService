package org.LogStorageService.controller;

import lombok.extern.slf4j.Slf4j;
import org.LogStorageService.models.LogEvent;
import org.LogStorageService.service.LogQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    /**
     * No null handling needed for input parameters as @PathVariable and @RequestParam annotations
     * internally handles it and throws BadRequestException in case of null paramters
     */
    @RequestMapping("/service/{serviceName}")
    public List<LogEvent> getByService(@PathVariable String serviceName,
                                       @RequestParam long startTimeMillis,
                                       @RequestParam long endTimeMillis) {
        validateTimeRange(startTimeMillis, endTimeMillis);
        log.info("Fetching logs for service={} startTimeMillis={} endTimeMillis={}", serviceName, startTimeMillis, endTimeMillis);
        List<LogEvent> logs = logQueryService.getLogsByService(serviceName, startTimeMillis, endTimeMillis);
        log.debug("Found {} logs for service={}", logs.size(), serviceName);
        return logs;
    }

    @GetMapping("/service/{serviceName}/host/{hostId}")
    public List<LogEvent> getByServiceAndHost(@PathVariable String serviceName,
                                              @PathVariable String hostId,
                                              @RequestParam long startTimeMillis,
                                              @RequestParam long endTimeMillis) {
        validateTimeRange(startTimeMillis, endTimeMillis);
        log.info("Fetching logs for service={} host={} startTimeMillis={} endTimeMillis={}", serviceName, hostId, startTimeMillis, endTimeMillis);
        List<LogEvent> logs = logQueryService.getLogsByServiceAndHost(serviceName, hostId, startTimeMillis, endTimeMillis);
        log.debug("Found {} logs for service={} host={}", logs.size(), serviceName, hostId);
        return logs;
    }

    @GetMapping("/host/{hostId}")
    public List<LogEvent> getByHost(@PathVariable String hostId,
                                    @RequestParam long startTimeMillis,
                                    @RequestParam long endTimeMillis) {
        validateTimeRange(startTimeMillis, endTimeMillis);
        log.info("Fetching logs for host={} startTimeMillis={} endTimeMillis={}", hostId, startTimeMillis, endTimeMillis);
        List<LogEvent> logs = logQueryService.getLogsByHost(hostId, startTimeMillis, endTimeMillis);
        log.debug("Found {} logs for host={}", logs.size(), hostId);
        return logs;
    }

    /**
     * Helper to validate that "startTimeMillis" <= "endTimeMillis".
     * Throws BadRequestStatusException if invalid.
     */
    private void validateTimeRange(long startTimeMillis, long endTimeMillis) {
        if (startTimeMillis > endTimeMillis) {
            log.warn("Invalid time range: startTimeMillis {} > endTimeMillis {}", startTimeMillis, endTimeMillis);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time range: 'startTimeMillis' must be <= 'endTimeMillis'");
        }
    }
}
