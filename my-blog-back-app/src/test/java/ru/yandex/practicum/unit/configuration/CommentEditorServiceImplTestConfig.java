package ru.yandex.practicum.unit.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.comment.editor.CommentEditorService;
import ru.yandex.practicum.service.post.comment.editor.CommentEditorServiceImpl;

@Configuration
public class CommentEditorServiceImplTestConfig {

    @Bean
    public CommentEditorService commentEditorService(
            CommentRepository commentRepository
    ) {
        return new CommentEditorServiceImpl(commentRepository);
    }
}
