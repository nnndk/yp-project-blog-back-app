package ru.yandex.practicum.integration.configuration.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.repository.post.JbcNativePostRepository;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.repository.post.comment.JbcNativeCommentRepository;
import ru.yandex.practicum.repository.post.image.JbcNativePostImageRepository;
import ru.yandex.practicum.repository.post.image.PostImageRepository;
import ru.yandex.practicum.repository.post.tag.JbcNativeTagRepository;
import ru.yandex.practicum.repository.post.tag.TagRepository;
import ru.yandex.practicum.tools.ArrayDiffTools;
import ru.yandex.practicum.tools.DateTimeTools;

@Configuration
public class AllRepositoryConfig {

    @Bean
    public PostRepository postRepository(JdbcTemplate jdbcTemplate, DateTimeTools dateTimeTools) {
        return new JbcNativePostRepository(jdbcTemplate, dateTimeTools);
    }

    @Bean
    public TagRepository tagRepository(
            JdbcTemplate jdbcTemplate,
            DateTimeTools dateTimeTools,
            ArrayDiffTools arrayDiffTools
    ) {
        return new JbcNativeTagRepository(jdbcTemplate, dateTimeTools, arrayDiffTools);
    }

    @Bean
    public CommentRepository commentRepository(JdbcTemplate jdbcTemplate, DateTimeTools dateTimeTools) {
        return new JbcNativeCommentRepository(jdbcTemplate, dateTimeTools);
    }

    @Bean
    public PostImageRepository postImageRepository(JdbcTemplate jdbcTemplate, DateTimeTools dateTimeTools) {
        return new JbcNativePostImageRepository(jdbcTemplate, dateTimeTools);
    }
}
