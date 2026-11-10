package ru.yandex.practicum.unit.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.comment.provider.CommentProviderService;
import ru.yandex.practicum.service.post.comment.provider.CommentProviderServiceImpl;

@Configuration
public class CommentProviderServiceImplTestConfig {

    @Bean
    public CommentProviderService commentProviderService(
            CommentRepository commentRepository
    ) {
        return new CommentProviderServiceImpl(commentRepository);
    }
}
