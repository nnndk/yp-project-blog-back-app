package ru.yandex.practicum.unit.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.service.storage.name.UUIDUniqueFileNameService;
import ru.yandex.practicum.service.storage.name.UniqueFileNameService;

@Configuration
public class UUIDUniqueFileNameServiceTestConfig {

    @Bean
    public UniqueFileNameService uniqueFileNameService() {
        return new UUIDUniqueFileNameService();
    }
}
