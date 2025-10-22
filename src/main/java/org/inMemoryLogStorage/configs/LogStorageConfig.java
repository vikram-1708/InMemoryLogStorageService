package org.inMemoryLogStorage.configs;

import lombok.extern.slf4j.Slf4j;
import org.inMemoryLogStorage.storage.InMemoryLogStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@SuppressWarnings("unused")
@Slf4j
public class LogStorageConfig {

    private static final Logger log = LoggerFactory.getLogger(LogStorageConfig.class);

    @Bean
    public InMemoryLogStorage inMemoryLogStorage() {
        log.debug("Creating Bean for InMemoryLogStorage");
        return new InMemoryLogStorage();
    }
}
