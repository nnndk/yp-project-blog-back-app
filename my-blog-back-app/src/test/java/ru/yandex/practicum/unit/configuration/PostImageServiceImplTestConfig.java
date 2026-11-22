package ru.yandex.practicum.unit.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.repository.post.image.PostImageRepository;
import ru.yandex.practicum.service.post.image.PostImageService;
import ru.yandex.practicum.service.post.image.PostImageServiceImpl;
import ru.yandex.practicum.service.storage.FileStorageService;

@Configuration
public class PostImageServiceImplTestConfig {

    @Bean
    public PostImageService postImageService(
            FileStorageService fileStorageService,
            PostImageRepository postImageRepository
    ) {
        return new PostImageServiceImpl(fileStorageService, postImageRepository);
    }
}
