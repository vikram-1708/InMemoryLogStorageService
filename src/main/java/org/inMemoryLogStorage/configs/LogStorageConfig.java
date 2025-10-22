package org.inMemoryLogStorage.configs;

import org.inMemoryLogStorage.storage.InMemoryLogStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@SuppressWarnings("unused")
public class LogStorageConfig {

    @Bean
    public InMemoryLogStorage inMemoryLogStorage() {
        return new InMemoryLogStorage();
    }
}
