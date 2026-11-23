package ru.yandex.practicum.integration.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.exceptions.post.image.PostImageNotFoundException;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.model.post.PostImage;
import ru.yandex.practicum.repository.post.image.PostImageRepository;
import ru.yandex.practicum.service.post.image.PostImageService;
import ru.yandex.practicum.service.storage.FileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostImageServiceRepositoryIT {

    @Value("${storage.local.root}")
    private String storageRoot;

    @Value("${storage.local.bucket.postimage}")
    private String bucketName;

    private Path bucket;

    @Autowired private PostImageService postImageService;
    @Autowired private PostImageRepository postImageRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Post post;

    @BeforeAll
    void initBucket() {
        bucket = Paths.get(storageRoot)
                .toAbsolutePath()
                .normalize()
                .resolve(bucketName)
                .normalize();

        if (!Files.isDirectory(bucket)) {
            throw new RuntimeException("Bucket is not a directory: " + bucket.toAbsolutePath());
        }
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("insert into posts(id, title, text) values (?, ?, ?)", 66L, "title1", "text1");
        jdbcTemplate.update("insert into posts(id, title, text) values (?, ?, ?)", 67L, "title2", "text2");
        post = new Post(66L, "title1", "text1", 0);
    }

    @AfterAll
    void clearTestStorage() throws IOException {
        try (Stream<Path> paths = Files.walk(bucket)) {
            paths.filter(p -> !p.equals(bucket))
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    private MockMultipartFile imageFile(String content) {
        return new MockMultipartFile("image", "image.png", "image/png", content.getBytes());
    }

    private long countImages() {
        return jdbcTemplate.queryForObject("select count(*) from post_images", Long.class);
    }


    @Nested
    class UpdatePostImage {

        @Test
        void firstImage_createsDbRecordAndSavesFile() {
            postImageService.updatePostImage(post, imageFile("data"));

            PostImage saved = postImageRepository.findPostImageByPostId(66L).orElseThrow();
            assertThat(saved.getOriginFileName()).isEqualTo("image.png");
            assertThat(countImages()).isEqualTo(1);
            assertThat(fileStorageService.getFile(saved.getStorageFileId())).isPresent();
        }

        @Test
        void existingImage_updatesDbRecorfAndReplacesFile() {
            postImageService.updatePostImage(post, imageFile("old data"));
            String oldStorageId = postImageRepository.findPostImageByPostId(66L)
                    .orElseThrow().getStorageFileId();

            postImageService.updatePostImage(post, imageFile("new data"));

            PostImage updated = postImageRepository.findPostImageByPostId(66L).orElseThrow();
            assertThat(updated.getStorageFileId()).isNotEqualTo(oldStorageId);
            assertThat(countImages()).isEqualTo(1);
        }

        @Test
        void oldFileDeleted_newFileExists() {
            postImageService.updatePostImage(post, imageFile("old data"));
            String oldStorageId = postImageRepository.findPostImageByPostId(66L)
                    .orElseThrow().getStorageFileId();

            postImageService.updatePostImage(post, imageFile("new data"));
            String newStorageId = postImageRepository.findPostImageByPostId(66L)
                    .orElseThrow().getStorageFileId();

            assertThat(fileStorageService.getFile(oldStorageId)).isEmpty();
            assertThat(fileStorageService.getFile(newStorageId)).isPresent();
        }

        @Test
        void differentPosts_isolated() {
            Post otherPost = new Post(67L, "title2", "text2", 0);

            postImageService.updatePostImage(post, imageFile("data1"));
            postImageService.updatePostImage(otherPost, imageFile("data2"));

            assertThat(countImages()).isEqualTo(2);
            assertThat(postImageRepository.findPostImageByPostId(66L)).isPresent();
            assertThat(postImageRepository.findPostImageByPostId(67L)).isPresent();
        }
    }


    @Nested
    class GetPostImage {

        @Test
        void noImage_returnsEmptyResource() {
            Resource result = postImageService.getPostImage(post);

            assertThat(result).isInstanceOf(ByteArrayResource.class);
            assertThat(((ByteArrayResource) result).getByteArray()).isEmpty();
        }

        @Test
        void fullChain_returnsContent() {
            postImageService.updatePostImage(post, imageFile("data"));

            Resource result = postImageService.getPostImage(post);

            assertThat(result).isInstanceOf(ByteArrayResource.class);
            assertThat(((ByteArrayResource) result).getByteArray()).isEqualTo("data".getBytes());
        }

        @Test
        void imageInDbButMissingInStorage_returnsEmptyResource() {
            jdbcTemplate.update(
                    "insert into post_images(post_id, origin_filename, storage_file_id) values (?, ?, ?)",
                    66L, "image.png", "non-existent-storage-id");

            Resource result = postImageService.getPostImage(post);

            assertThat(result).isInstanceOf(ByteArrayResource.class);
            assertThat(((ByteArrayResource) result).getByteArray()).isEmpty();
        }
    }


    @Nested
    class DeletePostImage {

        @Test
        void deletesFromDbAndStorage() {
            postImageService.updatePostImage(post, imageFile("data"));
            String storageId = postImageRepository.findPostImageByPostId(66L)
                    .orElseThrow().getStorageFileId();

            postImageService.deletePostImage(post);

            assertThat(postImageRepository.findPostImageByPostId(66L)).isEmpty();
            assertThat(countImages()).isZero();
            assertThat(fileStorageService.getFile(storageId)).isEmpty();
        }

        @Test
        void noImage_throws() {
            assertThatThrownBy(() -> postImageService.deletePostImage(post))
                    .isInstanceOf(PostImageNotFoundException.class)
                    .hasMessageContaining("66");
        }

        @Test
        void doesNotTouchOtherPosts() {
            Post otherPost = new Post(67L, "title2", "text2", 0);
            postImageService.updatePostImage(post, imageFile("data1"));
            postImageService.updatePostImage(otherPost, imageFile("data2"));

            postImageService.deletePostImage(post);

            assertThat(postImageRepository.findPostImageByPostId(66L)).isEmpty();
            assertThat(postImageRepository.findPostImageByPostId(67L)).isPresent();
            assertThat(countImages()).isEqualTo(1);
        }
    }
}
