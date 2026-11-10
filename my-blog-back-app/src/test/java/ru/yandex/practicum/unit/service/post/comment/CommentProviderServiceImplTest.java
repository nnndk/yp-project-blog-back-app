package ru.yandex.practicum.unit.service.post.comment;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.exceptions.post.comment.CommentNotFountException;
import ru.yandex.practicum.model.post.Comment;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.comment.provider.CommentProviderService;
import ru.yandex.practicum.unit.configuration.CommentProviderServiceImplTestConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(CommentProviderServiceImplTestConfig.class)
public class CommentProviderServiceImplTest {

    @MockitoBean
    private CommentRepository commentRepository;

    @Autowired
    private CommentProviderService service;


    @Nested
    class GetPostCommentModelById {

        @Test
        void notFound_throws() {
            long postId = 1;
            long commentId = 1;
            Post post = new Post(postId, "title1", "text1", 5);

            when(commentRepository.findCommentByPostIdByCommentId(postId, commentId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getPostCommentModelById(post, commentId))
                    .isInstanceOf(CommentNotFountException.class)
                    .hasMessage((new CommentNotFountException(postId, commentId)).getMessage());
        }

        @Test
        void found_returnsComment() {
            long postId = 1;
            long commentId = 1;
            Post post = new Post(postId, "title1", "text1", 5);
            Comment comment = new Comment(commentId, "comment text 1", postId);

            when(commentRepository.findCommentByPostIdByCommentId(postId, commentId))
                    .thenReturn(Optional.of(comment));

            assertThat(service.getPostCommentModelById(post, commentId)).isSameAs(comment);
        }
    }


    @Nested
    class GetPostComments {

        @Test
        void noComments_returnsEmptyList() {
            long postId = 1;
            Post post = new Post(postId, "title1", "text1", 5);

            when(commentRepository.findAllPostComments(postId)).thenReturn(List.of());

            List<CommentDto> result = service.getPostComments(post);

            verify(commentRepository).findAllPostComments(postId);
            assertThat(result).isEmpty();
        }

        @Test
        void commentsExist_returnsMappedDtos() {
            long postId = 1;
            Post post = new Post(postId, "title1", "text1", 5);
            Comment comment1 = new Comment(2, "comment text 1", postId);
            Comment comment2 = new Comment(100L, "comment text 2", postId);

            when(commentRepository.findAllPostComments(postId))
                    .thenReturn(List.of(comment1, comment2));

            List<CommentDto> result = service.getPostComments(post);

            verify(commentRepository).findAllPostComments(postId);
            assertThat(result).hasSize(2);

            CommentDto first = result.getFirst();
            assertThat(first.id()).isEqualTo(2);
            assertThat(first.text()).isEqualTo("comment text 1");
            assertThat(first.postId()).isEqualTo(postId);

            CommentDto second = result.get(1);
            assertThat(second.id()).isEqualTo(100L);
            assertThat(second.text()).isEqualTo("comment text 2");
            assertThat(second.postId()).isEqualTo(postId);
        }
    }


    @Nested
    class GetPostCommentById {

        @Test
        void found_returnsMappedDto() {
            long postId = 1;
            long commentId = 1;
            Post post = new Post(postId, "title1", "text1", 5);
            Comment comment = new Comment(commentId, "comment text 1", postId);

            when(commentRepository.findCommentByPostIdByCommentId(postId, commentId))
                    .thenReturn(Optional.of(comment));

            CommentDto result = service.getPostCommentById(post, commentId);

            verify(commentRepository).findCommentByPostIdByCommentId(postId, commentId);
            assertThat(result.id()).isEqualTo(commentId);
            assertThat(result.text()).isEqualTo("comment text 1");
            assertThat(result.postId()).isEqualTo(postId);
        }
    }
}
