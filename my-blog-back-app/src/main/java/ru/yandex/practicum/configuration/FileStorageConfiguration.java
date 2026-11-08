package ru.yandex.practicum.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.service.storage.FileStorageService;
import ru.yandex.practicum.service.storage.LocalFileStorageService;
import ru.yandex.practicum.service.storage.name.UniqueFileNameService;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfiguration {

    @Bean
    public FileStorageService postImageFileStorageService(
            @Value("${storage.local.root}") String storageRootDirName,
            @Value("${storage.local.bucket.postimage}") String bucketDirName,
            UniqueFileNameService uniqueFileNameService
    ) {
        Path storageRootPath= Paths.get(storageRootDirName).toAbsolutePath().normalize();
        return new LocalFileStorageService(storageRootPath, bucketDirName, uniqueFileNameService);
    }
}
