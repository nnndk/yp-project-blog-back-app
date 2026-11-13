package ru.yandex.practicum.integration.configuration.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.repository.post.JdbcNativePostRepository;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.repository.post.comment.JdbcNativeCommentRepository;
import ru.yandex.practicum.repository.post.image.JdbcNativePostImageRepository;
import ru.yandex.practicum.repository.post.image.PostImageRepository;
import ru.yandex.practicum.repository.post.tag.JdbcNativeTagRepository;
import ru.yandex.practicum.repository.post.tag.TagRepository;
import ru.yandex.practicum.tools.ArrayDiffTools;
import ru.yandex.practicum.tools.DateTimeTools;

@Configuration
public class AllRepositoryConfig {

    @Bean
    public PostRepository postRepository(JdbcTemplate jdbcTemplate, DateTimeTools dateTimeTools) {
        return new JdbcNativePostRepository(jdbcTemplate, dateTimeTools);
    }

    @Bean
    public TagRepository tagRepository(
            JdbcTemplate jdbcTemplate,
            DateTimeTools dateTimeTools,
            ArrayDiffTools arrayDiffTools
    ) {
        return new JdbcNativeTagRepository(jdbcTemplate, dateTimeTools, arrayDiffTools);
    }

    @Bean
    public CommentRepository commentRepository(JdbcTemplate jdbcTemplate, DateTimeTools dateTimeTools) {
        return new JdbcNativeCommentRepository(jdbcTemplate, dateTimeTools);
    }

    @Bean
    public PostImageRepository postImageRepository(JdbcTemplate jdbcTemplate, DateTimeTools dateTimeTools) {
        return new JdbcNativePostImageRepository(jdbcTemplate, dateTimeTools);
    }
}
