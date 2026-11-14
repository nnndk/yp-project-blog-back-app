package ru.yandex.practicum.integration.configuration;

import org.h2.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.yandex.practicum.integration.configuration.internal.AllRepositoryConfig;
import ru.yandex.practicum.integration.configuration.internal.ToolConfig;
import ru.yandex.practicum.integration.configuration.implementation.InMemoryFileStorageService;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.repository.post.image.PostImageRepository;
import ru.yandex.practicum.repository.post.tag.TagRepository;
import ru.yandex.practicum.service.post.comment.editor.CommentEditorService;
import ru.yandex.practicum.service.post.comment.editor.CommentEditorServiceImpl;
import ru.yandex.practicum.service.post.comment.provider.CommentProviderService;
import ru.yandex.practicum.service.post.comment.provider.CommentProviderServiceImpl;
import ru.yandex.practicum.service.post.editor.PostEditorService;
import ru.yandex.practicum.service.post.editor.PostEditorServiceImpl;
import ru.yandex.practicum.service.post.image.PostImageService;
import ru.yandex.practicum.service.post.image.PostImageServiceImpl;
import ru.yandex.practicum.service.post.provider.PostProviderService;
import ru.yandex.practicum.service.post.provider.PostProviderServiceImpl;
import ru.yandex.practicum.service.post.tag.TagService;
import ru.yandex.practicum.service.post.tag.TagServiceImpl;
import ru.yandex.practicum.service.storage.FileStorageService;
import ru.yandex.practicum.service.storage.name.UUIDUniqueFileNameService;
import ru.yandex.practicum.service.storage.name.UniqueFileNameService;
import ru.yandex.practicum.tools.ArrayDiffTools;
import ru.yandex.practicum.tools.SearchTools;

import javax.sql.DataSource;

@Configuration
@Import({ToolConfig.class, AllRepositoryConfig.class})
public class ServiceRepositoryTestConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean public UniqueFileNameService uniqueFileNameService() {
        return new UUIDUniqueFileNameService();
    }

    @Bean public FileStorageService fileStorageService() {
        return new InMemoryFileStorageService();
    }

    @Bean
    public TagService tagService(TagRepository tagRepository, ArrayDiffTools arrayDiffTools) {
        return new TagServiceImpl(tagRepository, arrayDiffTools);
    }

    @Bean
    public CommentProviderService commentProviderService(CommentRepository commentRepository) {
        return new CommentProviderServiceImpl(commentRepository);
    }

    @Bean
    public CommentEditorService commentEditorService(CommentRepository commentRepository) {
        return new CommentEditorServiceImpl(commentRepository);
    }

    @Bean
    public PostProviderService postProviderService(
            TagService tagService,
            PostRepository postRepository,
            CommentRepository commentRepository,
            SearchTools searchTools
    ) {
        return new PostProviderServiceImpl(tagService, postRepository, commentRepository, searchTools);
    }

    @Bean
    public PostImageService postImageService(
            FileStorageService fileStorageService,
            PostImageRepository postImageRepository
    ) {
        return new PostImageServiceImpl(fileStorageService, postImageRepository);
    }

    @Bean
    public PostEditorService postEditorService(
            PostProviderService postProviderService,
            PostImageService postImageService,
            PostRepository postRepository,
            TagService tagService,
            CommentRepository commentRepository
    ) {
        return new PostEditorServiceImpl(postProviderService, postImageService, postRepository, tagService, commentRepository);
    }
}
