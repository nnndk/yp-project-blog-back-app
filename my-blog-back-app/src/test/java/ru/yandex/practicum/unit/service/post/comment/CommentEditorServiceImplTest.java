package ru.yandex.practicum.unit.service.post.comment;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;
import ru.yandex.practicum.model.post.Comment;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.comment.editor.CommentEditorService;
import ru.yandex.practicum.unit.configuration.CommentEditorServiceImplTestConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(CommentEditorServiceImplTestConfig.class)
public class CommentEditorServiceImplTest {

    @MockitoBean
    private CommentRepository commentRepository;

    @Autowired
    private CommentEditorService service;


    @Nested
    class CreatePostComment {

        @Test
        void createsCommentAndReturnsDto() {
            long postId = 1;
            long commentId = 2;
            String text = "comment text 1";
            Post post = new Post(postId, "title1", "text1", 5);
            CreateCommentDto dto = new CreateCommentDto(text, postId);
            Comment created = new Comment(commentId, text, postId);

            when(commentRepository.createPostComment(postId, dto)).thenReturn(created);

            CommentDto result = service.createPostComment(post, dto);

            verify(commentRepository).createPostComment(postId, dto);
            assertThat(result.id()).isEqualTo(commentId);
            assertThat(result.text()).isEqualTo(text);
            assertThat(result.postId()).isEqualTo(postId);
        }

        @Test
        void usesPostIdFromPostNotFromDto() {
            long postId = 1;
            long dtoPostId = 91;
            long commentId = 2;
            String text = "comment text 1";
            Post post = new Post(postId, "title1", "text1", 5);
            CreateCommentDto dto = new CreateCommentDto(text, dtoPostId);
            Comment created = new Comment(commentId, text, postId);

            when(commentRepository.createPostComment(eq(postId), any(CreateCommentDto.class)))
                    .thenReturn(created);

            service.createPostComment(post, dto);

            verify(commentRepository).createPostComment(eq(postId), any(CreateCommentDto.class));
            verifyNoMoreInteractions(commentRepository);
        }
    }


    @Nested
    class UpdatePostComment {

        @Test
        void updatesCommentAndReturnsDto() {
            long postId = 1;
            long commentId = 2;
            String text = "updated text";
            Post post = new Post(postId, "title1", "text1", 5);
            Comment comment = new Comment(commentId, "old text", postId);
            UpdateCommentDto dto = new UpdateCommentDto(commentId, text, postId);
            Comment updated = new Comment(commentId, text, postId);

            when(commentRepository.updatePostComment(postId, commentId, dto)).thenReturn(updated);

            CommentDto result = service.updatePostComment(post, comment, dto);

            verify(commentRepository).updatePostComment(postId, commentId, dto);
            assertThat(result.id()).isEqualTo(commentId);
            assertThat(result.text()).isEqualTo(text);
            assertThat(result.postId()).isEqualTo(postId);
        }

        @Test
        void usesIdsFromModelsNotFromDto() {
            long postId = 1;
            long commentId = 2;
            long dtoPostId = 111;
            long dtoCommentId = 222;
            String text = "updated text";
            Post post = new Post(postId, "title1", "text1", 5);
            Comment comment = new Comment(commentId, "old text", postId);
            UpdateCommentDto dto = new UpdateCommentDto(dtoCommentId, text, dtoPostId);
            Comment updated = new Comment(commentId, text, postId);

            when(commentRepository.updatePostComment(eq(postId), eq(commentId), any(UpdateCommentDto.class)))
                    .thenReturn(updated);

            service.updatePostComment(post, comment, dto);

            verify(commentRepository).updatePostComment(eq(postId), eq(commentId), any(UpdateCommentDto.class));
            verifyNoMoreInteractions(commentRepository);
        }
    }


    @Nested
    class DeletePostCommentById {

        @Test
        void deletesByPostIdAndCommentId() {
            long postId = 1;
            long commentId = 2;
            Post post = new Post(postId, "title1", "text1", 5);
            Comment comment = new Comment(commentId, "comment text 1", postId);

            service.deletePostCommentById(post, comment);

            verify(commentRepository).deleteCommentByPostIdByCommentId(postId, commentId);
            verifyNoMoreInteractions(commentRepository);
        }

        @Test
        void usesIdsFromModelsNotFromDto() {
            long postId = 1;
            long commentId = 2;
            Post post = new Post(postId, "title1", "text1", 5);
            Comment comment = new Comment(commentId, "comment text 1", postId);

            service.deletePostCommentById(post, comment);

            verify(commentRepository).deleteCommentByPostIdByCommentId(eq(postId), eq(commentId));
        }
    }
}
