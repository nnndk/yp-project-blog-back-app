package ru.yandex.practicum.service.storage.name;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UUIDUniqueFileNameService implements UniqueFileNameService {

    @Override
    public String generateFileName() {
        return UUID.randomUUID().toString();
    }
}