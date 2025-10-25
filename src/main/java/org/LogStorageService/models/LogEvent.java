package org.LogStorageService.models;

import lombok.Getter;

@Getter
public class LogEvent {

    private final long timestamp; // 8bytes
    private final String serviceName; // 40 bytes
    private final String hostId; // 40 bytes
    private final String logMessage; // 100 bytes

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
