package ru.yandex.practicum.integration.configuration;

import org.h2.Driver;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import ru.yandex.practicum.WebConfiguration;
import ru.yandex.practicum.controller.PostCommentController;
import ru.yandex.practicum.controller.PostController;
import ru.yandex.practicum.controller.common.ControllerVerifyHelper;
import ru.yandex.practicum.service.post.comment.editor.CommentEditorService;
import ru.yandex.practicum.service.post.comment.provider.CommentProviderService;
import ru.yandex.practicum.service.post.editor.PostEditorService;
import ru.yandex.practicum.service.post.image.PostImageService;
import ru.yandex.practicum.service.post.provider.PostProviderService;
import ru.yandex.practicum.service.storage.FileStorageService;
import ru.yandex.practicum.service.storage.LocalFileStorageService;
import ru.yandex.practicum.service.storage.name.UniqueFileNameService;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

@Configuration
@Import(WebConfiguration.class)
public class WebTestConfig implements DisposableBean {

    // беру WebConfiguration из реального приложения, подменяю источник данных на тестовый и добавляю бины с контроллерами

    private Path storageRoot;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:testdb3;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @EventListener
    public void populate(ContextRefreshedEvent event) {
        DataSource dataSource = event.getApplicationContext().getBean(DataSource.class);

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("test-schema.sql"));
        populator.setSqlScriptEncoding("UTF-8");
        populator.execute(dataSource);
    }

    @Bean
    @Primary
    public FileStorageService postImageFileStorageService(
            UniqueFileNameService uniqueFileNameService
    ) throws IOException {
        // временная тестовая директория в корне приложения
        this.storageRoot = Files.createTempDirectory("test-storage");
        Path bucket = storageRoot.resolve("test-post-image");
        Files.createDirectories(bucket);

        return new LocalFileStorageService(
                storageRoot.toString(), "test-post-image", uniqueFileNameService);
    }

    @Bean
    @Primary
    public PostController postController(
            ControllerVerifyHelper controllerVerifyHelper,
            PostProviderService postProviderService,
            PostImageService postImageService,
            PostEditorService postEditorService
    ) {
        return new PostController(controllerVerifyHelper, postProviderService, postImageService, postEditorService);
    }

    @Bean
    @Primary
    public PostCommentController postCommentController(
            ControllerVerifyHelper controllerVerifyHelper,
            CommentProviderService commentProviderService,
            CommentEditorService commentEditorService,
            PostProviderService postProviderService
    ) {
        return new PostCommentController(controllerVerifyHelper, commentProviderService, commentEditorService, postProviderService);
    }

    @Override
    public void destroy() throws Exception {
        cleanupStorage();
    }

    private void cleanupStorage () throws IOException {
        if (storageRoot == null || !Files.exists(storageRoot)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(storageRoot)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }
}
