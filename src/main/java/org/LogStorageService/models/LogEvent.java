package org.LogStorageService.models;

import lombok.Getter;

@Getter
public class LogEvent {

    private final long timestamp;
    private final String serviceName;
    private final String hostId;
    private final String logMessage;

    public LogEvent(long timestamp, String serviceName, String hostId, String logMessage) {
        this.timestamp = timestamp;
        this.serviceName = serviceName;
        this.hostId = hostId;
        this.logMessage = logMessage;
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "timestamp=" + timestamp +
                ", serviceName='" + serviceName + '\'' +
                ", hostId='" + hostId + '\'' +
                ", logMessage='" + logMessage + '\'' +
                '}';
    }
}
