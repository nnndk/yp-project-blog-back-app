package ru.yandex.practicum.integration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;
import ru.yandex.practicum.exceptions.post.comment.CommentNotFoundException;
import ru.yandex.practicum.integration.configuration.ServiceRepositoryTestConfig;
import ru.yandex.practicum.model.post.Comment;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.service.post.comment.editor.CommentEditorService;
import ru.yandex.practicum.service.post.comment.provider.CommentProviderService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(ServiceRepositoryTestConfig.class)
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class CommentServiceRepositoryIT {

    @Autowired private CommentEditorService commentEditorService;
    @Autowired private CommentProviderService commentProviderService;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Post post;
    private Post otherPost;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("insert into posts(id, title, text) values (?, ?, ?)", 66L, "title1", "text1");
        jdbcTemplate.update("insert into posts(id, title, text) values (?, ?, ?)", 67L, "title2", "text2");
        post = new Post(66L, "title1", "text1", 0);
        otherPost = new Post(67L, "title2", "text2", 0);
    }

    private long countComments() {
        return jdbcTemplate.queryForObject("select count(*) from comments", Long.class);
    }

    private Comment getComment(CommentDto dto) {
        return commentProviderService.getPostCommentModelById(post, dto.id());
    }


    @Nested
    class CreatePostComment {

        @Test
        void persistsWithCorrectPostId() {
            CommentDto created = commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 1", 66L));

            assertThat(created.id()).isPositive();
            assertThat(created.text()).isEqualTo("comment text 1");
            assertThat(created.postId()).isEqualTo(66L);

            CommentDto found = commentProviderService.getPostCommentById(post, created.id());
            assertThat(found.text()).isEqualTo("comment text 1");
        }

        @Test
        void multipleComments() {
            commentEditorService.createPostComment(post, new CreateCommentDto("comment text 1", 66L));
            commentEditorService.createPostComment(post, new CreateCommentDto("comment text 2", 66L));

            assertThat(countComments()).isEqualTo(2);
            assertThat(commentProviderService.getPostComments(post)).hasSize(2);
        }
    }


    @Nested
    class UpdatePostComment {

        @Test
        void updatesTextInDb() {
            CommentDto created = commentEditorService.createPostComment(post,
                    new CreateCommentDto("old text", 66L));
            Comment comment = getComment(created);

            commentEditorService.updatePostComment(post, comment,
                    new UpdateCommentDto(comment.getId(), "new text", 66L));

            assertThat(commentProviderService.getPostCommentById(post, created.id()).text())
                    .isEqualTo("new text");
        }

        @Test
        void usesCommentIdFromModel_notFromDto() {
            CommentDto c1 = commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 1", 66L));
            CommentDto c2 = commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 2", 66L));
            Comment comment1 = getComment(c1);

            commentEditorService.updatePostComment(post, comment1,
                    new UpdateCommentDto(999L, "new text", 66L));

            assertThat(commentProviderService.getPostCommentById(post, c1.id()).text())
                    .isEqualTo("new text");
            assertThat(commentProviderService.getPostCommentById(post, c2.id()).text())
                    .isEqualTo("comment text 2");
        }

        @Test
        void usesPostIdFromModel_notFromDto() {
            CommentDto c1 = commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 1", 66L));
            Comment comment1 = getComment(c1);

            commentEditorService.updatePostComment(post, comment1,
                    new UpdateCommentDto(c1.id(), "new text", 999L));

            assertThat(commentProviderService.getPostCommentById(post, c1.id()).text())
                    .isEqualTo("new text");
        }

        @Test
        void doesNotToucHOtherComments() {
            CommentDto c1 = commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 1", 66L));
            CommentDto c2 = commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 2", 66L));

            commentEditorService.updatePostComment(post, getComment(c1),
                    new UpdateCommentDto(c1.id(), "new text", 66L));

            assertThat(commentProviderService.getPostCommentById(post, c2.id()).text())
                    .isEqualTo("comment text 2");
        }
    }


    @Nested
    class DeletePostCommentById {

        @Test
        void deletesCorrectComment() {
            CommentDto c1 = commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 1", 66L));
            CommentDto c2 = commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 2", 66L));

            commentEditorService.deletePostCommentById(post, getComment(c1));

            assertThat(countComments()).isEqualTo(1);
            assertThatThrownBy(() -> commentProviderService.getPostCommentById(post, c1.id()))
                    .isInstanceOf(CommentNotFoundException.class);
            assertThat(commentProviderService.getPostCommentById(post, c2.id()).text())
                    .isEqualTo("comment text 2");
        }

        @Test
        void doesNotTouchCommentsOfOtherPost() {
            CommentDto c1 = commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 1", 66L));
            CommentDto c2 = commentEditorService.createPostComment(otherPost,
                    new CreateCommentDto("comment text 2", 67L));

            commentEditorService.deletePostCommentById(post, getComment(c1));

            assertThat(commentProviderService.getPostCommentById(otherPost, c2.id()).text())
                    .isEqualTo("comment text 2");
        }
    }


    @Nested
    class GetPostComments {

        @Test
        void emptyList() {
            assertThat(commentProviderService.getPostComments(post)).isEmpty();
        }

        @Test
        void returnsAllForPost() {
            commentEditorService.createPostComment(post, new CreateCommentDto("comment text 1", 66L));
            commentEditorService.createPostComment(post, new CreateCommentDto("comment text 2", 66L));
            commentEditorService.createPostComment(otherPost, new CreateCommentDto("comment text 3", 67L));

            List<CommentDto> result = commentProviderService.getPostComments(post);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(CommentDto::text)
                    .containsExactly("comment text 1", "comment text 2");
            assertThat(result).allMatch(dto -> dto.postId() == 66L);
        }
    }


    @Nested
    class GetPostCommentById {

        @Test
        void found_returnsCorrectDto() {
            CommentDto created = commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 1", 66L));

            CommentDto result = commentProviderService.getPostCommentById(post, created.id());

            assertThat(result.id()).isEqualTo(created.id());
            assertThat(result.text()).isEqualTo("comment text 1");
            assertThat(result.postId()).isEqualTo(66L);
        }

        @Test
        void notFound_throws() {
            assertThatThrownBy(() -> commentProviderService.getPostCommentById(post, 99L))
                    .isInstanceOf(CommentNotFoundException.class)
                    .hasMessageContaining("66")
                    .hasMessageContaining("99");
        }

        @Test
        void wrongPost_throws() {
            CommentDto created = commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 1", 66L));

            assertThatThrownBy(() -> commentProviderService.getPostCommentById(otherPost, created.id()))
                    .isInstanceOf(CommentNotFoundException.class);
        }
    }
}
