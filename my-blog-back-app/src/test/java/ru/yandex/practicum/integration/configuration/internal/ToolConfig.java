package ru.yandex.practicum.integration.configuration.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.tools.ArrayDiffTools;
import ru.yandex.practicum.tools.DateTimeTools;
import ru.yandex.practicum.tools.SearchTools;

@Configuration
public class ToolConfig {

    @Bean
    public DateTimeTools dateTimeTools() {
        return new DateTimeTools();
    }

    @Bean
    public ArrayDiffTools arrayDiffTools() {
        return new ArrayDiffTools();
    }

    @Bean
    public SearchTools searchTools() {
        return new SearchTools();
    }
}
