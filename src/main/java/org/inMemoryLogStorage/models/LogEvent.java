package org.inMemoryLogStorage.models;

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

    public long getTimestamp() {
        return timestamp;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getHostId() {
        return hostId;
    }

    public String getLogMessage() {
        return logMessage;
    }
}
