package ru.yandex.practicum.service.storage;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.exceptions.file.DirectoryNotFountException;
import ru.yandex.practicum.exceptions.file.FileNotFountException;
import ru.yandex.practicum.service.storage.name.UniqueFileNameService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class LocalFileStorageService implements FileStorageService {

    private final Path storageRootPath;
    private final String bucketDirName;
    private final Path bucketPath;

    private final UniqueFileNameService uniqueFileNameService;


    public LocalFileStorageService(
            Path storageRootPath,
            String bucketDirName,
            UniqueFileNameService uniqueFileNameService
    ) {
        this.storageRootPath = storageRootPath;
        this.bucketDirName = bucketDirName;
        this.bucketPath = storageRootPath.resolve(bucketDirName).normalize();

        this.uniqueFileNameService = uniqueFileNameService;

        validateBucketName();
        ensureBucketExists();
    }

    private void validateBucketName() {
        if (bucketDirName == null || !bucketDirName.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Invalid bucket dir name in config: " + bucketDirName);
        }
    }

    private void ensureBucketExists() {
        if (!Files.isDirectory(bucketPath)) {
            throw new DirectoryNotFountException(bucketDirName, storageRootPath.toString());
        }
    }

    @Override
    public boolean isFileExists(String filename) {
        Path file = getFilePath(filename);
        return Files.isRegularFile(file);
    }

    @Override
    public Optional<Resource> getFile(String filename) {
        Path filePath = getFilePath(filename);
        if (!Files.isRegularFile(filePath)) {
            return Optional.empty();
        }

        try {
            byte[] content = Files.readAllBytes(filePath);
            return Optional.of(new ByteArrayResource(content));
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Failed to get file `%s`", filename), e);
        }
    }

    @Override
    public String saveFile(MultipartFile file) {
        String uniqueFileName = uniqueFileNameService.generateFileName();
        Path pathToSaveFile =  bucketPath.resolve(uniqueFileName).normalize();

        try {
            file.transferTo(pathToSaveFile);
            return uniqueFileName;
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Failed to save file to bucket `%s`", bucketDirName), e);
        }
    }

    @Override
    public void deleteFile(String filename) {
        Path filePath = getFilePath(filename);
        if (!Files.isRegularFile(filePath)) {
            throw new FileNotFountException(bucketDirName, filename);
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Failed to delete file `%s`", filename), e);
        }
    }

    private Path getFilePath(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Invalid filename: " + filename);
        }

        return bucketPath.resolve(filename).normalize();
    }
}
