package org.LogStorageService.configs;

import lombok.extern.slf4j.Slf4j;
import org.LogStorageService.storage.InMemoryLogStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@SuppressWarnings("unused")
@Slf4j
public class LogStorageConfig {

    @Bean
    public InMemoryLogStorage inMemoryLogStorage() {
        log.debug("Creating Bean for InMemoryLogStorage");
        try {
            return new InMemoryLogStorage();
        } catch (Exception e) {
            log.error("Failed to create InMemoryLogStorage bean", e);
            throw e;
        }
    }
}
