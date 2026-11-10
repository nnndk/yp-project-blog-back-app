package ru.yandex.practicum.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface FileStorageService {

    Optional<Resource> getFile(String filename);

    String saveFile(MultipartFile file);

    void deleteFile(String filename);
}