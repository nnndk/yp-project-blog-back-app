package ru.yandex.practicum.integration.configuration;

import org.h2.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
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

import javax.sql.DataSource;

@Configuration
public class RepositoryTestConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:usersdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DateTimeTools dateTimeTools() {
        return new DateTimeTools();
    }

    @Bean
    public ArrayDiffTools arrayDiffTools() {
        return new ArrayDiffTools();
    }

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
