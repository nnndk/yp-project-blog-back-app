package ru.yandex.practicum.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;
import ru.yandex.practicum.integration.configuration.RepositoryTestConfig;
import ru.yandex.practicum.model.post.Comment;
import ru.yandex.practicum.repository.post.comment.CommentRepository;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(RepositoryTestConfig.class)
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class JbcNativeCommentRepositoryIT {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("insert into posts(id, title, text) values (?, ?, ?)", 1L, "title1", "text1");
        jdbcTemplate.update("insert into posts(id, title, text) values (?, ?, ?)", 2L, "title2", "text2");
    }

    private long insertComment(long postId, String text) {
        jdbcTemplate.update(
                "insert into comments(text, post_id) values (?, ?)", text, postId);
        return jdbcTemplate.queryForObject(
                "select max(id) from comments where post_id = ?", Long.class, postId);
    }


    @Nested
    class FindCommentByPostIdByCommentId {

        @Test
        void found_returnsComment() {
            long commentId = insertComment(1L, "comment text 1");
            Optional<Comment> result = commentRepository.findCommentByPostIdByCommentId(1L, commentId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(commentId);
            assertThat(result.get().getText()).isEqualTo("comment text 1");
            assertThat(result.get().getPostId()).isEqualTo(1L);
        }

        @Test
        void notFound_returnsEmpty() {
            long postId = 1L;
            long commentId = 99L;

            assertThat(commentRepository.findCommentByPostIdByCommentId(postId, commentId)).isEmpty();
        }

        @Test
        void wrongPostId_returnsEmpty() {
            long commentId = insertComment(1L, "comment text 1");
            assertThat(commentRepository.findCommentByPostIdByCommentId(2L, commentId)).isEmpty();
        }

        @Test
        void wrongCommentId_returnsEmpty() {
            insertComment(1L, "comment text 1");
            assertThat(commentRepository.findCommentByPostIdByCommentId(1L, 99L)).isEmpty();
        }
    }


    @Nested
    class FindAllPostComments {

        @Test
        void noComments_returnsEmptyList() {
            assertThat(commentRepository.findAllPostComments(1L)).isEmpty();
        }

        @Test
        void returnsOnlyCommentsOfGivenPost() {
            long c1 = insertComment(1L, "comment text 1");
            insertComment(1L, "comment text 2");
            insertComment(2L, "comment text 3");

            List<Comment> result = commentRepository.findAllPostComments(1L);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Comment::getText)
                    .containsExactly("comment text 1", "comment text 2");
            assertThat(result).allMatch(c -> c.getPostId() == 1L);
            assertThat(result.getFirst().getId()).isEqualTo(c1);
        }

        @Test
        void orderedById() {
            long c1 = insertComment(1L, "comment text 1");
            long c2 = insertComment(1L, "comment text 2");
            long c3 = insertComment(1L, "comment text 3");

            assertThat(commentRepository.findAllPostComments(1L))
                    .extracting(Comment::getId)
                    .containsExactly(c1, c2, c3);
        }
    }


    @Nested
    class CountPostCommentsSingle {

        @Test
        void noComments_returnsZero() {
            assertThat(commentRepository.countPostComments(1L)).isZero();
        }

        @Test
        void returnsCountForGivenPost() {
            insertComment(1L, "comment text 1");
            insertComment(1L, "comment text 2");
            insertComment(2L, "comment text 3");

            assertThat(commentRepository.countPostComments(1L)).isEqualTo(2);
            assertThat(commentRepository.countPostComments(2L)).isEqualTo(1);
        }
    }


    @Nested
    class CountPostCommentsBatch {

        @Test
        void emptyArray_returnsEmptyMap() {
            assertThat(commentRepository.countPostComments(new long[0])).isEmpty();
        }

        @Test
        void nullArray_returnsEmptyMap() {
            assertThat(commentRepository.countPostComments(null)).isEmpty();
        }

        @Test
        void returnsCountsForAllRequestedPosts() {
            insertComment(1L, "comment text 1");
            insertComment(1L, "comment text 2");
            insertComment(2L, "comment text 3");

            Map<Long, Integer> result = commentRepository.countPostComments(new long[]{1L, 2L});

            assertThat(result).containsEntry(1L, 2).containsEntry(2L, 1);
            assertThat(result).hasSize(2);
        }

        @Test
        void postsWithoutComments_absentFromMap() {
            insertComment(1L, "comment text 1");
            Map<Long, Integer> result = commentRepository.countPostComments(new long[]{1L, 2L});

            assertThat(result).containsEntry(1L, 1).doesNotContainKey(2L);
        }

        @Test
        void duplicatePostIds_returnsUniqueKeys() {
            insertComment(1L, "comment text 1");
            Map<Long, Integer> result = commentRepository.countPostComments(new long[]{1L, 1L, 1L});

            assertThat(result).containsEntry(1L, 1).hasSize(1);
        }
    }


    @Nested
    class CreatePostComment {

        @Test
        void createsAndReturnsComment() {
            CreateCommentDto dto = new CreateCommentDto("comment text 1", 1L);

            Comment created = commentRepository.createPostComment(1L, dto);

            assertThat(created.getId()).isPositive();
            assertThat(created.getText()).isEqualTo("comment text 1");
            assertThat(created.getPostId()).isEqualTo(1L);

            Optional<Comment> persisted = commentRepository.findCommentByPostIdByCommentId(
                    1L, created.getId());
            assertThat(persisted).isPresent();
            assertThat(persisted.get().getText()).isEqualTo("comment text 1");
        }

        @Test
        void fillsCreatedAndModifiedAt() {
            CreateCommentDto dto = new CreateCommentDto("comment text 1", 1L);
            OffsetDateTime before = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(1);

            Comment created = commentRepository.createPostComment(1L, dto);

            Timestamp createdAt = jdbcTemplate.queryForObject(
                    "select created_at from comments where id = ?", Timestamp.class, created.getId());
            Timestamp modifiedAt = jdbcTemplate.queryForObject(
                    "select modified_at from comments where id = ?", Timestamp.class, created.getId());

            assertThat(createdAt).isNotNull();
            assertThat(modifiedAt).isNotNull();
            assertThat(createdAt.toInstant()).isAfterOrEqualTo(before.toInstant());
            assertThat(modifiedAt.toInstant()).isEqualTo(createdAt.toInstant());
        }
    }


    @Nested
    class UpdatePostComment {

        @Test
        void updatesTextAndReturnsComment() {
            long commentId = insertComment(1L, "old text");
            UpdateCommentDto dto = new UpdateCommentDto(commentId, "new text", 1L);

            Comment updated = commentRepository.updatePostComment(1L, commentId, dto);

            assertThat(updated.getId()).isEqualTo(commentId);
            assertThat(updated.getText()).isEqualTo("new text");
            assertThat(updated.getPostId()).isEqualTo(1L);
        }

        @Test
        void persistsUpdatedText() {
            long commentId = insertComment(1L, "old text");
            UpdateCommentDto dto = new UpdateCommentDto(commentId, "new text", 1L);

            commentRepository.updatePostComment(1L, commentId, dto);

            String text = jdbcTemplate.queryForObject(
                    "select text from comments where id = ?", String.class, commentId);
            assertThat(text).isEqualTo("new text");
        }

        @Test
        void updatesModifiedAt() {
            long commentId = insertComment(1L, "old text");
            Timestamp before = jdbcTemplate.queryForObject(
                    "select modified_at from comments where id = ?", Timestamp.class, commentId);

            try { Thread.sleep(20); } catch (InterruptedException ignored) { }

            commentRepository.updatePostComment(1L, commentId,
                    new UpdateCommentDto(commentId, "new text", 1L));

            Timestamp after = jdbcTemplate.queryForObject(
                    "select modified_at from comments where id = ?", Timestamp.class, commentId);
            assertThat(after.toInstant()).isAfter(before.toInstant());
        }

        @Test
        void doesNotTouchOtherComments() {
            long target = insertComment(1L, "old text");
            long other = insertComment(1L, "other text");

            commentRepository.updatePostComment(1L, target,
                    new UpdateCommentDto(target, "new text", 1L));

            String otherText = jdbcTemplate.queryForObject(
                    "select text from comments where id = ?", String.class, other);
            assertThat(otherText).isEqualTo("other text");
        }
    }


    @Nested
    class DeleteCommentByPostIdByCommentId {

        @Test
        void deletesMatchingComment() {
            long commentId = insertComment(1L, "comment text 1");

            commentRepository.deleteCommentByPostIdByCommentId(1L, commentId);

            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from comments where id = ?", Integer.class, commentId))
                    .isZero();
        }

        @Test
        void doesNotDeleteCommentsOfOtherPost() {
            long c1 = insertComment(1L, "comment text 1");
            long c2 = insertComment(2L, "comment text 2");

            commentRepository.deleteCommentByPostIdByCommentId(1L, c1);

            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from comments where id = ?", Integer.class, c2))
                    .isEqualTo(1);
        }

        @Test
        void doesNotDeleteOtherCommentsOfSamePost() {
            long target = insertComment(1L, "comment text 1");
            long other = insertComment(1L, "comment text 2");

            commentRepository.deleteCommentByPostIdByCommentId(1L, target);

            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from comments where id = ?", Integer.class, other))
                    .isEqualTo(1);
        }

        @Test
        void nonExistent_noException() {
            commentRepository.deleteCommentByPostIdByCommentId(1L, 99L);
        }
    }
}
