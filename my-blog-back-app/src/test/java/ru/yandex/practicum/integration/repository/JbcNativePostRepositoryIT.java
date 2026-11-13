package ru.yandex.practicum.integration.repository;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;
import ru.yandex.practicum.integration.configuration.RepositoryTestConfig;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.repository.post.PostRepository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(RepositoryTestConfig.class)
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class JbcNativePostRepositoryIT {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    private void insertPost(long id, String title) {
        jdbcTemplate.update("insert into posts(id, title, text) values (?, ?, ?)",
                id, title, "text" + id);
    }

    private void insertPost(long id) {
        insertPost(id, "title" + id);
    }

    private long insertTag(String tag) {
        jdbcTemplate.update("insert into tags(tag) values (?)", tag);
        return jdbcTemplate.queryForObject("select id from tags where tag = ?", Long.class, tag);
    }

    private void link(long postId, long tagId) {
        jdbcTemplate.update("insert into posts_tags_link(post_id, tag_id) values (?, ?)", postId, tagId);
    }

    private void insertComment(long postId, String text) {
        jdbcTemplate.update("insert into comments(text, post_id) values (?, ?)", text, postId);
    }

    private void insertImage(long postId) {
        jdbcTemplate.update(
                "insert into post_images(post_id, origin_filename, storage_file_id) values (?, ?, ?)",
                postId, "image" + postId + ".png", "storage-" + postId);
    }

    private long countLinks(long postId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from posts_tags_link where post_id = ?", Long.class, postId);
    }

    private long countComments(long postId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from comments where post_id = ?", Long.class, postId);
    }

    private long countImages(long postId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from post_images where post_id = ?", Long.class, postId);
    }

    private boolean tagExists(String tag) {
        return jdbcTemplate.queryForObject(
                "select count(*) from tags where tag = ?", Long.class, tag) > 0;
    }


    @Nested
    class FindPostById {

        @Test
        void found_returnsPost() {
            insertPost(1L, "title1");

            Optional<Post> result = postRepository.findPostById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getTitle()).isEqualTo("title1");
            assertThat(result.get().getText()).isEqualTo("text1");
        }

        @Test
        void notFound_returnsEmpty() {
            assertThat(postRepository.findPostById(99L)).isEmpty();
        }
    }


    @Nested
    class CountAllPosts {

        @Test
        void empty_returnsZero() {
            assertThat(postRepository.countAllPosts()).isZero();
        }

        @Test
        void returnsTotalCount() {
            insertPost(1L);
            insertPost(2L);
            insertPost(3L);

            assertThat(postRepository.countAllPosts()).isEqualTo(3);
        }
    }


    @Nested
    class CountSearchedPostsByTitleSubstring {

        @Test
        void noMatches_returnsZero() {
            insertPost(1L, "Название поста 1");

            assertThat(postRepository.countSearchedPostsByTitleSubstring("xyz")).isZero();
        }

        @Test
        void cyrillicSubstring_matches() {
            insertPost(1L, "Название поста 1");
            insertPost(2L, "Другое");

            assertThat(postRepository.countSearchedPostsByTitleSubstring("аз")).isEqualTo(1);
        }

        @Test
        void ilike_caseInsensitive() {
            insertPost(1L, "Hello World");

            assertThat(postRepository.countSearchedPostsByTitleSubstring("hello")).isEqualTo(1);
            assertThat(postRepository.countSearchedPostsByTitleSubstring("HELLO")).isEqualTo(1);
        }

        @Test
        void partialMatch() {
            insertPost(1L, "abc");
            insertPost(2L, "xabcx");
            insertPost(3L, "xyz");

            assertThat(postRepository.countSearchedPostsByTitleSubstring("abc")).isEqualTo(2);
        }
    }


    @Nested
    class CountSearchedPostsByTags {

        @Test
        void noPosts_returnsZero() {
            assertThat(postRepository.countSearchedPostsByTags(List.of("tag_1"))).isZero();
        }

        @Test
        void singleTag() {
            long t1 = insertTag("tag_1");
            insertPost(1L);
            insertPost(2L);
            link(1L, t1);

            assertThat(postRepository.countSearchedPostsByTags(List.of("tag_1"))).isEqualTo(1);
        }

        @Test
        void allTagsMustMatch() {
            long t1 = insertTag("tag_1");
            long t2 = insertTag("tag_2");
            insertPost(1L);
            link(1L, t1);
            link(1L, t2);
            insertPost(2L);
            link(2L, t1);
            insertPost(3L);
            link(3L, t2);

            assertThat(postRepository.countSearchedPostsByTags(List.of("tag_1", "tag_2"))).isEqualTo(1);
        }

        @Test
        void oneTagMissing_returnsZero() {
            long t1 = insertTag("tag_1");
            insertPost(1L);
            link(1L, t1);

            assertThat(postRepository.countSearchedPostsByTags(List.of("tag_1", "tag_2"))).isZero();
        }

        @Test
        void postWithoutTags_notCounted() {
            insertPost(1L);

            assertThat(postRepository.countSearchedPostsByTags(List.of("tag_1"))).isZero();
        }
    }


    @Nested
    class CountSearchedPostsByTagsAndTitleSubstring {

        @Test
        void bothMatch_returnsCount() {
            long t1 = insertTag("tag_1");
            insertPost(1L, "Название поста 1");
            link(1L, t1);
            insertPost(2L, "Название поста 2");
            insertPost(3L, "Другое");
            link(3L, t1);

            assertThat(postRepository.countSearchedPostsByTagsAndTitleSubstring(
                    List.of("tag_1"), "Название")).isEqualTo(1);
        }

        @Test
        void tagsMatchButTitleDoesNot_returnsZero() {
            long t1 = insertTag("tag_1");
            insertPost(1L, "Другое");
            link(1L, t1);

            assertThat(postRepository.countSearchedPostsByTagsAndTitleSubstring(
                    List.of("tag_1"), "Название")).isZero();
        }

        @Test
        void titleMatchesButTagsDoNot_returnsZero() {
            long t1 = insertTag("tag_1");
            insertPost(1L, "Название поста 1");
            link(1L, t1);
            insertPost(2L, "Название поста 2");

            assertThat(postRepository.countSearchedPostsByTagsAndTitleSubstring(
                    List.of("tag_2"), "Название")).isZero();
        }
    }


    @Nested
    class CountAndDataAgreement {

        @Test
        void cyrillic_countEqualsDataSize() {
            insertPost(1L, "Название поста 1");
            insertPost(2L, "Название поста 2");
            insertPost(3L, "Другое");

            long count = postRepository.countSearchedPostsByTitleSubstring("аз");
            List<Post> data = postRepository.searchPostsByTitleSubstringPaginated("аз", 0L, 100L);

            assertThat(count).isEqualTo(2);
            assertThat(data).hasSize((int) count);
        }

        @Test
        void tags_countEqualsDataSize() {
            long t1 = insertTag("tag_1");
            long t2 = insertTag("tag_2");
            insertPost(1L);
            link(1L, t1);
            link(1L, t2);
            insertPost(2L);
            link(2L, t1);

            long count = postRepository.countSearchedPostsByTags(List.of("tag_1", "tag_2"));
            List<Post> data = postRepository.searchPostsByTagsPaginated(
                    List.of("tag_1", "tag_2"), 0L, 100L);

            assertThat(count).isEqualTo(1);
            assertThat(data).hasSize((int) count);
        }

        @Test
        void tagsAndTitle_countEqualsDataSize() {
            long t1 = insertTag("tag_1");
            insertPost(1L, "Название 1");
            link(1L, t1);
            insertPost(2L, "Название 2");
            link(2L, t1);
            insertPost(3L, "Другое");
            link(3L, t1);

            long count = postRepository.countSearchedPostsByTagsAndTitleSubstring(
                    List.of("tag_1"), "Название");
            List<Post> data = postRepository.searchPostsByTagsAndTitleSubstringPaginated(
                    List.of("tag_1"), "Название", 0L, 100L);

            assertThat(count).isEqualTo(2);
            assertThat(data).hasSize((int) count);
        }
    }


    @Nested
    class GetAllPostsPaginated {

        @Test
        void empty_returnsEmptyList() {
            assertThat(postRepository.getAllPostsPaginated(0L, 10L)).isEmpty();
        }

        @Test
        void returnsFirstPage() {
            for (long i = 1; i <= 5; i++) insertPost(i);

            List<Post> page = postRepository.getAllPostsPaginated(0L, 3L);

            assertThat(page).hasSize(3);
            assertThat(page).extracting(Post::getId).containsExactly(1L, 2L, 3L);
        }

        @Test
        void returnsSecondPage() {
            for (long i = 1; i <= 5; i++) insertPost(i);

            List<Post> page = postRepository.getAllPostsPaginated(3L, 3L);

            assertThat(page).extracting(Post::getId).containsExactly(4L, 5L);
        }

        @Test
        void offsetBeyondTotal_returnsEmpty() {
            insertPost(1L);

            assertThat(postRepository.getAllPostsPaginated(100L, 10L)).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, -100L, Long.MIN_VALUE})
        void negativeOffset_throws(long offset) {
            assertThatThrownBy(() -> postRepository.getAllPostsPaginated(offset, 10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("`offset` cannot be less than 0");
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, Long.MIN_VALUE})
        void invalidLimit_throws(long limit) {
            assertThatThrownBy(() -> postRepository.getAllPostsPaginated(0L, limit))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("`limit` cannot be less than 1");
        }
    }


    @Nested
    class SearchPostsByTagsPaginated {

        @Test
        void singleTag() {
            long t1 = insertTag("tag_1");
            insertPost(1L);
            link(1L, t1);
            insertPost(2L);

            List<Post> result = postRepository.searchPostsByTagsPaginated(List.of("tag_1"), 0L, 10L);

            assertThat(result).extracting(Post::getId).containsExactly(1L);
        }

        @Test
        void allTagsMustMatch() {
            long t1 = insertTag("tag_1");
            long t2 = insertTag("tag_2");
            insertPost(1L);
            link(1L, t1);
            link(1L, t2);
            insertPost(2L);
            link(2L, t1);

            List<Post> result = postRepository.searchPostsByTagsPaginated(
                    List.of("tag_1", "tag_2"), 0L, 10L);

            assertThat(result).extracting(Post::getId).containsExactly(1L);
        }

        @Test
        void pagination() {
            long t1 = insertTag("tag_1");
            for (long i = 1; i <= 5; i++) {
                insertPost(i);
                link(i, t1);
            }

            assertThat(postRepository.searchPostsByTagsPaginated(List.of("tag_1"), 0L, 2L))
                    .extracting(Post::getId).containsExactly(1L, 2L);
            assertThat(postRepository.searchPostsByTagsPaginated(List.of("tag_1"), 2L, 2L))
                    .extracting(Post::getId).containsExactly(3L, 4L);
            assertThat(postRepository.searchPostsByTagsPaginated(List.of("tag_1"), 4L, 2L))
                    .extracting(Post::getId).containsExactly(5L);
        }
    }


    @Nested
    class SearchPostsByTitleSubstringPaginated {

        @Test
        void cyrillicSubstring() {
            insertPost(1L, "Название поста 1");
            insertPost(2L, "Название поста 2");
            insertPost(3L, "Другое");

            List<Post> result = postRepository.searchPostsByTitleSubstringPaginated("аз", 0L, 10L);

            assertThat(result).extracting(Post::getId).containsExactly(1L, 2L);
        }

        @Test
        void ilike_caseInsensitive() {
            insertPost(1L, "Hello World");

            assertThat(postRepository.searchPostsByTitleSubstringPaginated("hello", 0L, 10L))
                    .extracting(Post::getId).containsExactly(1L);
        }

        @Test
        void pagination() {
            for (long i = 1; i <= 5; i++) insertPost(i, "prefix" + i);

            assertThat(postRepository.searchPostsByTitleSubstringPaginated("prefix", 0L, 2L))
                    .extracting(Post::getId).containsExactly(1L, 2L);
            assertThat(postRepository.searchPostsByTitleSubstringPaginated("prefix", 2L, 2L))
                    .extracting(Post::getId).containsExactly(3L, 4L);
        }

        @Test
        void offsetBeyondTotal_returnsEmpty() {
            insertPost(1L, "prefix");

            assertThat(postRepository.searchPostsByTitleSubstringPaginated("prefix", 100L, 10L)).isEmpty();
        }
    }


    @Nested
    class SearchPostsByTagsAndTitleSubstringPaginated {

        @Test
        void bothFiltersApplied() {
            long t1 = insertTag("tag_1");
            insertPost(1L, "Название 1");
            link(1L, t1);
            insertPost(2L, "Название 2");
            link(2L, t1);
            insertPost(3L, "Другое");
            link(3L, t1);
            insertPost(4L, "Название 3");

            List<Post> result = postRepository.searchPostsByTagsAndTitleSubstringPaginated(
                    List.of("tag_1"), "Название", 0L, 10L);

            assertThat(result).extracting(Post::getId).containsExactly(1L, 2L);
        }

        @Test
        void pagination() {
            long t1 = insertTag("tag_1");
            for (long i = 1; i <= 5; i++) {
                insertPost(i, "Название " + i);
                link(i, t1);
            }

            assertThat(postRepository.searchPostsByTagsAndTitleSubstringPaginated(
                    List.of("tag_1"), "Название", 0L, 2L))
                    .extracting(Post::getId).containsExactly(1L, 2L);
            assertThat(postRepository.searchPostsByTagsAndTitleSubstringPaginated(
                    List.of("tag_1"), "Название", 2L, 2L))
                    .extracting(Post::getId).containsExactly(3L, 4L);
        }
    }


    @Nested
    class CreatePost {

        @Test
        void createsAndReturnsPost() {
            Post created = postRepository.createPost(new CreatePostDto("title1", "text1"));

            assertThat(created.getId()).isPositive();
            assertThat(created.getTitle()).isEqualTo("title1");
            assertThat(created.getText()).isEqualTo("text1");
            assertThat(created.getLikesCount()).isZero();
        }

        @Test
        void fillsCreatedAndModifiedAt() {
            OffsetDateTime before = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(1);

            Post created = postRepository.createPost(new CreatePostDto("title1", "text1"));

            Timestamp createdAt = jdbcTemplate.queryForObject(
                    "select created_at from posts where id = ?", Timestamp.class, created.getId());
            Timestamp modifiedAt = jdbcTemplate.queryForObject(
                    "select modified_at from posts where id = ?", Timestamp.class, created.getId());

            assertThat(createdAt).isNotNull();
            assertThat(modifiedAt).isNotNull();
            assertThat(createdAt.toInstant()).isAfterOrEqualTo(before.toInstant());
            assertThat(modifiedAt.toInstant()).isEqualTo(createdAt.toInstant());
        }
    }


    @Nested
    class UpdatePost {

        @Test
        void updatesTitleAndText() {
            insertPost(1L, "old title");

            postRepository.updatePost(1L, new UpdatePostDto(1L, "new title", "new text"));

            Post updated = postRepository.findPostById(1L).orElseThrow();
            assertThat(updated.getTitle()).isEqualTo("new title");
            assertThat(updated.getText()).isEqualTo("new text");
        }

        @Test
        void updatesModifiedAt() {
            insertPost(1L);
            Timestamp before = jdbcTemplate.queryForObject(
                    "select modified_at from posts where id = ?", Timestamp.class, 1L);

            try { Thread.sleep(20); } catch (InterruptedException ignored) { }

            postRepository.updatePost(1L, new UpdatePostDto(1L, "new title", "new text"));

            Timestamp after = jdbcTemplate.queryForObject(
                    "select modified_at from posts where id = ?", Timestamp.class, 1L);
            assertThat(after.toInstant()).isAfter(before.toInstant());
        }

        @Test
        void nonExistent_noException() {
            postRepository.updatePost(99L, new UpdatePostDto(99L, "new title", "new text"));

            assertThat(postRepository.findPostById(99L)).isEmpty();
        }
    }


    @Nested
    class DeletePost {

        @Test
        void deletesPost() {
            insertPost(1L);

            postRepository.deletePost(1L);

            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from posts where id = ?", Long.class, 1L)).isZero();
        }

        @Test
        void cascadesTagsLinksCommentsImages() {
            long t1 = insertTag("tag_1");
            insertPost(1L);
            link(1L, t1);
            insertComment(1L, "comment text 1");
            insertImage(1L);

            postRepository.deletePost(1L);

            assertThat(countLinks(1L)).isZero();
            assertThat(countComments(1L)).isZero();
            assertThat(countImages(1L)).isZero();
        }

        @Test
        void doesNotDeleteTagItself() {
            long t1 = insertTag("tag_1");
            insertPost(1L);
            link(1L, t1);

            postRepository.deletePost(1L);

            assertThat(tagExists("tag_1")).isTrue();
            assertThat(countLinks(1L)).isZero();
        }

        @Test
        void doesNotTouchOtherPosts() {
            insertPost(1L);
            insertPost(2L);

            postRepository.deletePost(1L);

            assertThat(postRepository.findPostById(1L)).isEmpty();
            assertThat(postRepository.findPostById(2L)).isPresent();
        }

        @Test
        void nonExistent_noException() {
            postRepository.deletePost(99L);
        }
    }


    @Nested
    class IncrementPostLikes {

        @Test
        void incrementsLikesAndReturnsNewCount() {
            insertPost(1L);

            int result = postRepository.incrementPostLikes(1L);

            assertThat(result).isEqualTo(1);
            assertThat(postRepository.findPostById(1L).orElseThrow().getLikesCount()).isEqualTo(1);
        }

        @Test
        void multipleIncrements() {
            insertPost(1L);

            postRepository.incrementPostLikes(1L);
            postRepository.incrementPostLikes(1L);
            int result = postRepository.incrementPostLikes(1L);

            assertThat(result).isEqualTo(3);
        }

        @Test
        void updatesModifiedAt() {
            insertPost(1L);
            Timestamp before = jdbcTemplate.queryForObject(
                    "select modified_at from posts where id = ?", Timestamp.class, 1L);

            try { Thread.sleep(20); } catch (InterruptedException ignored) { }

            postRepository.incrementPostLikes(1L);

            Timestamp after = jdbcTemplate.queryForObject(
                    "select modified_at from posts where id = ?", Timestamp.class, 1L);
            assertThat(after.toInstant()).isAfter(before.toInstant());
        }

        @Test
        void nonExistent_throws() {
            assertThatThrownBy(() -> postRepository.incrementPostLikes(99L))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void returnsIncrementedValue() {
            insertPost(66L);

            int currLikes1 = postRepository.incrementPostLikes(66L);
            int currLikes2 = postRepository.incrementPostLikes(66L);
            int currLikes3 = postRepository.incrementPostLikes(66L);
            int currLikes4 = postRepository.incrementPostLikes(66L);
            int currLikes5 = postRepository.incrementPostLikes(66L);

            assertThat(currLikes1).isEqualTo(1);
            assertThat(currLikes2).isEqualTo(2);
            assertThat(currLikes3).isEqualTo(3);
            assertThat(currLikes4).isEqualTo(4);
            assertThat(currLikes5).isEqualTo(5);

            assertThat(postRepository.findPostById(66L).orElseThrow().getLikesCount())
                    .isEqualTo(5);
        }
    }
}
