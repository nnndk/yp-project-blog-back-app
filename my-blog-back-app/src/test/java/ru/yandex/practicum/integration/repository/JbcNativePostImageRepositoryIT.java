package ru.yandex.practicum.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.dto.post.image.CreatePostImageDto;
import ru.yandex.practicum.dto.post.image.UpdatePostImageDto;
import ru.yandex.practicum.integration.configuration.RepositoryTestConfig;
import ru.yandex.practicum.model.post.PostImage;
import ru.yandex.practicum.repository.post.image.PostImageRepository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(RepositoryTestConfig.class)
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class JbcNativePostImageRepositoryIT {

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        insertPost(1L);
        insertPost(2L);
    }


    private void insertPost(long id) {
        jdbcTemplate.update("insert into posts(id, title, text) values (?, ?, ?)",
                id, "title" + id, "text" + id);
    }

    private void insertImage(long postId, String originFilename, String storageFileId) {
        jdbcTemplate.update(
                "insert into post_images(post_id, origin_filename, storage_file_id) values (?, ?, ?)",
                postId, originFilename, storageFileId);
    }

    private long countImages() {
        return jdbcTemplate.queryForObject("select count(*) from post_images", Long.class);
    }

    private boolean imageExists(long postId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from post_images where post_id = ?", Long.class, postId) > 0;
    }


    @Nested
    class FindPostImageByPostId {

        @Test
        void notFound_returnsEmpty() {
            assertThat(postImageRepository.findPostImageByPostId(1L)).isEmpty();
        }

        @Test
        void found_returnsAllFields() {
            insertImage(1L, "image.png", "storage-file-id-1");

            Optional<PostImage> result = postImageRepository.findPostImageByPostId(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getPostId()).isEqualTo(1L);
            assertThat(result.get().getOriginFileName()).isEqualTo("image.png");
            assertThat(result.get().getStorageFileId()).isEqualTo("storage-file-id-1");
        }

        @Test
        void returnsOnlyImageOfGivenPost() {
            insertImage(1L, "image1.png", "storage-file-id-1");
            insertImage(2L, "image2.png", "storage-file-id-2");

            assertThat(postImageRepository.findPostImageByPostId(1L).orElseThrow().getOriginFileName())
                    .isEqualTo("image1.png");
            assertThat(postImageRepository.findPostImageByPostId(2L).orElseThrow().getOriginFileName())
                    .isEqualTo("image2.png");
        }
    }


    @Nested
    class CreatePostImage {

        @Test
        void createsAndReturnsImage() {
            CreatePostImageDto dto = new CreatePostImageDto("image.png", "storage-file-id-1");

            PostImage created = postImageRepository.createPostImage(1L, dto);

            assertThat(created.getPostId()).isEqualTo(1L);
            assertThat(created.getOriginFileName()).isEqualTo("image.png");
            assertThat(created.getStorageFileId()).isEqualTo("storage-file-id-1");
        }

        @Test
        void persistsInDb() {
            CreatePostImageDto dto = new CreatePostImageDto("image.png", "storage-file-id-1");

            postImageRepository.createPostImage(1L, dto);

            assertThat(imageExists(1L)).isTrue();
            assertThat(countImages()).isEqualTo(1);
        }

        @Test
        void fillsCreatedAndModifiedAt() {
            CreatePostImageDto dto = new CreatePostImageDto("image.png", "storage-file-id-1");
            OffsetDateTime before = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(1);

            postImageRepository.createPostImage(1L, dto);

            Timestamp createdAt = jdbcTemplate.queryForObject(
                    "select created_at from post_images where post_id = ?", Timestamp.class, 1L);
            Timestamp modifiedAt = jdbcTemplate.queryForObject(
                    "select modified_at from post_images where post_id = ?", Timestamp.class, 1L);

            assertThat(createdAt).isNotNull();
            assertThat(modifiedAt).isNotNull();
            assertThat(createdAt.toInstant()).isAfterOrEqualTo(before.toInstant());
            assertThat(modifiedAt.toInstant()).isEqualTo(createdAt.toInstant());
        }

        @Test
        void duplicateForSamePost_throws() {
            insertImage(1L, "old.png", "old-id");
            CreatePostImageDto dto = new CreatePostImageDto("new.png", "new-id");

            assertThatThrownBy(() -> postImageRepository.createPostImage(1L, dto))
                    .isInstanceOf(DuplicateKeyException.class);

            assertThat(countImages()).isEqualTo(1);
        }

        @Test
        void differentPosts_bothCreated() {
            postImageRepository.createPostImage(1L, new CreatePostImageDto("image1.png", "id-1"));
            postImageRepository.createPostImage(2L, new CreatePostImageDto("image2.png", "id-2"));

            assertThat(countImages()).isEqualTo(2);
            assertThat(imageExists(1L)).isTrue();
            assertThat(imageExists(2L)).isTrue();
        }
    }


    @Nested
    class UpdatePostImage {

        @Test
        void updatesAllFields() {
            insertImage(1L, "old.png", "old-id");
            UpdatePostImageDto dto = new UpdatePostImageDto("new.png", "new-id");

            postImageRepository.updatePostImage(1L, dto);

            PostImage updated = postImageRepository.findPostImageByPostId(1L).orElseThrow();
            assertThat(updated.getOriginFileName()).isEqualTo("new.png");
            assertThat(updated.getStorageFileId()).isEqualTo("new-id");
            assertThat(updated.getPostId()).isEqualTo(1L);
        }

        @Test
        void updatesModifiedAt() {
            insertImage(1L, "old.png", "old-id");
            Timestamp before = jdbcTemplate.queryForObject(
                    "select modified_at from post_images where post_id = ?", Timestamp.class, 1L);

            try { Thread.sleep(20); } catch (InterruptedException ignored) { }

            postImageRepository.updatePostImage(1L, new UpdatePostImageDto("new.png", "new-id"));

            Timestamp after = jdbcTemplate.queryForObject(
                    "select modified_at from post_images where post_id = ?", Timestamp.class, 1L);
            assertThat(after.toInstant()).isAfter(before.toInstant());
        }

        @Test
        void doesNotTouchOtherPosts() {
            insertImage(1L, "old1.png", "old-id-1");
            insertImage(2L, "old2.png", "old-id-2");

            postImageRepository.updatePostImage(1L, new UpdatePostImageDto("new1.png", "new-id-1"));

            PostImage untouched = postImageRepository.findPostImageByPostId(2L).orElseThrow();
            assertThat(untouched.getOriginFileName()).isEqualTo("old2.png");
            assertThat(untouched.getStorageFileId()).isEqualTo("old-id-2");
        }

        @Test
        void nonExistent_noException() {
            postImageRepository.updatePostImage(1L, new UpdatePostImageDto("new.png", "new-id"));

            assertThat(imageExists(1L)).isFalse();
        }
    }


    @Nested
    class DeletePostImage {

        @Test
        void deletesMatchingImage() {
            insertImage(1L, "image.png", "storage-file-id-1");

            postImageRepository.deletePostImage(1L);

            assertThat(imageExists(1L)).isFalse();
            assertThat(countImages()).isZero();
        }

        @Test
        void doesNotTouchOtherPosts() {
            insertImage(1L, "image1.png", "id-1");
            insertImage(2L, "image2.png", "id-2");

            postImageRepository.deletePostImage(1L);

            assertThat(imageExists(1L)).isFalse();
            assertThat(imageExists(2L)).isTrue();
        }

        @Test
        void nonExistent_noException() {
            postImageRepository.deletePostImage(1L);

            assertThat(countImages()).isZero();
        }
    }
}
