package ru.yandex.practicum.unit.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.editor.PostEditorService;
import ru.yandex.practicum.service.post.editor.PostEditorServiceImpl;
import ru.yandex.practicum.service.post.image.PostImageService;
import ru.yandex.practicum.service.post.provider.PostProviderService;
import ru.yandex.practicum.service.post.tag.TagService;

@Configuration
public class PostEditorServiceImplTestConfig {

    @Bean
    public PostEditorService postEditorService(
            PostProviderService postProviderService,
            PostImageService postImageService,
            TagService tagService,
            PostRepository postRepository,
            CommentRepository commentRepository
    ) {
        return new PostEditorServiceImpl(postProviderService, postImageService, postRepository, tagService, commentRepository);
    }
}
