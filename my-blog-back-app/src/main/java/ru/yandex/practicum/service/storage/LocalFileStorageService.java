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
import java.nio.file.Paths;
import java.util.Optional;

public class LocalFileStorageService implements FileStorageService {

    private final String bucketDirName;
    private final Path bucketPath;

    private final UniqueFileNameService uniqueFileNameService;


    public LocalFileStorageService(
            String storageRootDirName,
            String bucketDirName,
            UniqueFileNameService uniqueFileNameService
    ) {
        this.uniqueFileNameService = uniqueFileNameService;

        if (storageRootDirName == null || storageRootDirName.isBlank()) {
            throw new IllegalArgumentException("Storage root directory cannot be null or blank");
        }

        Path storageRootPath = Paths.get(storageRootDirName).toAbsolutePath().normalize();
        validateDirName(bucketDirName);
        this.bucketDirName = bucketDirName;
        this.bucketPath = storageRootPath.resolve(bucketDirName).normalize();
        ensureDirectoryExists(bucketPath, bucketDirName);
    }

    private void validateDirName(String dirName) {
        if (dirName == null || !dirName.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Invalid directory name in config: " + dirName);
        }
    }

    private void ensureDirectoryExists(Path path, String dirName) {
        if (!Files.isDirectory(path)) {
            throw new DirectoryNotFountException(dirName, path.toAbsolutePath().toString());
        }
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
