package ru.yandex.practicum.integration.configuration.implementation;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.service.storage.FileStorageService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryFileStorageService implements FileStorageService {
    private final Map<String, byte[]> files = new ConcurrentHashMap<>();

    @Override
    public Optional<Resource> getFile(String filename) {
        byte[] data = files.get(filename);
        return data == null ? Optional.empty() : Optional.of(new ByteArrayResource(data));
    }

    @Override
    public String saveFile(MultipartFile file) {
        String id = UUID.randomUUID().toString();
        try {
            files.put(id, file.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return id;
    }

    @Override
    public void deleteFile(String filename) {
        files.remove(filename);
    }
}
