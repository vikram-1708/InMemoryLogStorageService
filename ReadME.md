Project Structure:-

## Project Structure

- **models/**
    - `LogEvent.java` → POJO representing a log entry

- **storage/**
    - `InMemoryLogStorage.java` → Thread-safe in-memory log store

- **producers/**
    - `LogProducer.java` → Simulates log generation from services/hosts

- **service/**
    - `LogQueryService.java` → Business layer for querying logs

- **controller/**
    - `LogController.java` → REST endpoints for querying logs
    - 
- **schedulers/**
- `LogEvictionScheduler.java` → Periodic background task to clear expired logs


## Log Retention & Eviction
- By default, logs are retained for **1 hour** (`RETENTION_MILLIS`).
- Expired logs are removed using:
    - `LogEvictionScheduler` → a scheduled task that runs every **30 minute** and clears old logs.
    - Eviction is safe and does not block concurrent queries/writes.

API Curls for Queries:-

1. Logs of a given serviceName between time t1 and t2.
   ## curl --location 'http://localhost:8080/api/v1/logs/service/PaymentService?startTimeMillis=1761091200000&endTimeMillis=1761619199000' \
   --data ''

2. Logs of a given serviceName from a given hostId between time t1 and t2.
   ## curl --location 'http://localhost:8080/api/v1/logs/service/OrderService/host/order-node-1?startTimeMillis=1761091200000&endTimeMillis=1761619199000' \
   --data ''

3. Logs of a given hostId between time t1 and t2.
   ## curl --location 'http://localhost:8080/api/v1/logs/host/inventory-node-1?startTimeMillis=1761091200000&endTimeMillis=1761619199000' \
   --data ''