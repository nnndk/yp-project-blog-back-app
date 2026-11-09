package ru.yandex.practicum.unit.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.provider.PostProviderService;
import ru.yandex.practicum.service.post.provider.PostProviderServiceImpl;
import ru.yandex.practicum.service.post.tag.TagService;

@Configuration
public class PostProviderServiceImplTestConfig {

    @Bean
    public PostProviderService postProviderService(
            TagService tagService,
            PostRepository postRepository,
            CommentRepository commentRepository
    ) {
        return new PostProviderServiceImpl(tagService, postRepository, commentRepository);
    }
}
