package ru.yandex.practicum.tools;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class DateTimeTools {

    public OffsetDateTime getCurrentDateTime() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
