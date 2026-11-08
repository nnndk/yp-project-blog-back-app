package ru.yandex.practicum.service.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.exceptions.file.FileNotFountException;
import ru.yandex.practicum.service.file.name.UniqueFileNameService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
@Primary
public class LocalFileStorageService implements FileStorageService {

    private final Path storageRoot;

    private final UniqueFileNameService uniqueFileNameService;


    public LocalFileStorageService(
            @Value("${storage.local}") String storageDir,
            UniqueFileNameService uniqueFileNameService
    ) {
        this.storageRoot = Paths.get(storageDir).toAbsolutePath().normalize();
        this.uniqueFileNameService = uniqueFileNameService;
    }


    @Override
    public boolean isBucketExists(String bucket) {
        return Files.isDirectory(getBucketPath(bucket));
    }

    @Override
    public boolean isFileExists(String bucket, String filename) {
        Path file = getFilePath(bucket, filename);
        return Files.isRegularFile(file);
    }

    @Override
    public Optional<Resource> getFile(String bucket, String filename) {
        Path filePath = getFilePath(bucket, filename);
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
    public String saveFile(String bucket, MultipartFile file) {
        Path bucketPath = getBucketPath(bucket);
        String uniqueFileName = uniqueFileNameService.generateFileName();
        Path pathToSaveFile =  bucketPath.resolve(uniqueFileName).normalize();

        try {
            file.transferTo(pathToSaveFile);
            return uniqueFileName;
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Failed to save file to bucket `%s`", bucket), e);
        }
    }

    @Override
    public void deleteFile(String bucket, String filename) {
        Path filePath = getFilePath(bucket, filename);
        if (!Files.isRegularFile(filePath)) {
            throw new FileNotFountException(bucket, filename);
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Failed to delete file `%s`", filename), e);
        }
    }

    private Path getBucketPath(String bucket) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("Invalid bucket name: " + bucket);
        }

        return storageRoot.resolve(bucket).normalize();
    }

    private Path getFilePath(String bucket, String filename) {
        Path bucketPath = getBucketPath(bucket);
        return getFilePath(bucketPath, filename);
    }

    private Path getFilePath(Path bucket, String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Invalid filename: " + filename);
        }

        return bucket.resolve(filename).normalize();
    }
}
