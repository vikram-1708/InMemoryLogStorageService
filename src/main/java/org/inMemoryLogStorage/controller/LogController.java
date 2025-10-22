package org.inMemoryLogStorage.controller;

import org.inMemoryLogStorage.models.LogEvent;
import org.inMemoryLogStorage.service.LogQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logs")
@SuppressWarnings("unused")
public class LogController {

    private final LogQueryService queryService;

    public LogController(LogQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/service/{serviceName}")
    public List<LogEvent> getByService(@PathVariable String serviceName,
                                       @RequestParam long from,
                                       @RequestParam long to) {
        return queryService.getLogsByService(serviceName, from, to);
    }

    @GetMapping("/service/{serviceName}/host/{hostId}")
    public List<LogEvent> getByServiceAndHost(@PathVariable String serviceName,
                                              @PathVariable String hostId,
                                              @RequestParam long from,
                                              @RequestParam long to) {
        return queryService.getLogsByServiceAndHost(serviceName, hostId, from, to);
    }

    @GetMapping("/host/{hostId}")
    public List<LogEvent> getByHost(@PathVariable String hostId,
                                    @RequestParam long from,
                                    @RequestParam long to) {
        return queryService.getLogsByHost(hostId, from, to);
    }
}
