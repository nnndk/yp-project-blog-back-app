package ru.yandex.practicum.service.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface FileStorageService {

    boolean isBucketExists(String bucket);

    boolean isFileExists(String bucket, String filename);

    Optional<Resource> getFile(String bucket, String filename);

    String saveFile(String bucket, MultipartFile file);

    void deleteFile(String bucket, String filename);
}