package ru.yandex.practicum.unit.service.post;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.dto.post.*;
import ru.yandex.practicum.exceptions.post.image.PostImageNotFountException;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.editor.PostEditorService;
import ru.yandex.practicum.service.post.image.PostImageService;
import ru.yandex.practicum.service.post.provider.PostProviderService;
import ru.yandex.practicum.service.post.tag.TagService;
import ru.yandex.practicum.unit.configuration.PostEditorServiceImplTestConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(PostEditorServiceImplTestConfig.class)
public class PostEditorServiceImplTest {

    @MockitoBean
    private PostProviderService postProviderService;

    @MockitoBean
    private PostImageService postImageService;

    @MockitoBean
    private PostRepository postRepository;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private CommentRepository commentRepository;

    @Autowired
    private PostEditorService service;


    @Nested
    class CreatePost {

        @Test
        void createsPostAndReturnsDto() {
            long postId = 66L;
            String title = "title1";
            String text = "text1";
            String[] tags = {"tag_1", "tag_2"};
            int likesCount = 5;
            int commentsCount = 3;

            Post post = new Post(postId, title, text, likesCount);

            when(postRepository.createPost(new CreatePostDto(title, text))).thenReturn(post);
            when(commentRepository.countPostComments(postId)).thenReturn(commentsCount);

            PostDto result = service.createPost(new CreatePostWithTagsDto(title, text, tags));

            verify(postRepository).createPost(new CreatePostDto(title, text));
            verify(tagService).addTagsToPost(eq(post), aryEq(tags));
            verify(commentRepository).countPostComments(postId);
            verify(tagService, never()).getPostTags(any(Post.class));

            assertThat(result.id()).isEqualTo(postId);
            assertThat(result.title()).isEqualTo(title);
            assertThat(result.text()).isEqualTo(text);
            assertThat(result.tags()).containsExactly("tag_1", "tag_2");
            assertThat(result.likesCount()).isEqualTo(likesCount);
            assertThat(result.commentsCount()).isEqualTo(commentsCount);
        }

        @Test
        void emptyTags_returnsEmptyArray() {
            long postId = 66L;
            String[] tags = new String[0];
            Post post = new Post(postId, "title1", "text1", 5);

            when(postRepository.createPost(new CreatePostDto("title1", "text1"))).thenReturn(post);
            when(commentRepository.countPostComments(postId)).thenReturn(0);

            PostDto result = service.createPost(new CreatePostWithTagsDto("title1", "text1", tags));

            verify(tagService).addTagsToPost(eq(post), aryEq(tags));
            assertThat(result.tags()).isEmpty();
            assertThat(result.commentsCount()).isZero();
        }
    }

    @Nested
    class UpdatePost {

        @Test
        void updatesPostAndReturnsRichDto() {
            long postId = 66L;
            String title = "title1";
            String text = "text1";
            String[] tags = {"tag_1"};
            int likesCount = 5;
            int commentsCount = 3;

            Post post = new Post(postId, "oldTitle", "oldText", likesCount);
            UpdatePostWithTagsDto dto = new UpdatePostWithTagsDto(postId, title, text, tags);
            PostDto expected = new PostDto(postId, title, text, tags, likesCount, commentsCount);

            when(postProviderService.getRichPostById(post)).thenReturn(expected);

            PostDto result = service.updatePost(post, dto);

            verify(postRepository).updatePost(eq(postId), eq(new UpdatePostDto(postId, title, text)));
            verify(tagService).updatePostTags(eq(post), aryEq(tags));
            verify(postProviderService).getRichPostById(post);

            assertThat(result).isSameAs(expected);
        }

        @Test
        void usesPostIdNotDtoId() {
            long postId = 66L;
            long dtoId = 999L;
            String title = "title1";
            String text = "text1";

            Post post = new Post(postId, "oldTitle", "oldText", 5);
            UpdatePostWithTagsDto dto = new UpdatePostWithTagsDto(dtoId, title, text, new String[0]);
            PostDto expected = new PostDto(postId, title, text, new String[0], 5, 3);

            when(postProviderService.getRichPostById(post)).thenReturn(expected);

            service.updatePost(post, dto);

            verify(postRepository).updatePost(eq(postId), eq(new UpdatePostDto(postId, title, text)));
        }
    }


    @Nested
    class DeletePostById {

        @Test
        void deletesImageAndPost() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);

            service.deletePostById(post);

            verify(postImageService).deletePostImage(post);
            verify(postRepository).deletePost(postId);
        }

        @Test
        void imageNotFound_stillDeletesPost() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);

            doThrow(mock(PostImageNotFountException.class))
                    .when(postImageService).deletePostImage(post);

            service.deletePostById(post);

            verify(postImageService).deletePostImage(post);
            verify(postRepository).deletePost(postId);
        }

        @Test
        void otherException_propagatesAndDoesNotDeletePost() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);
            RuntimeException boom = new RuntimeException("boom");

            doThrow(boom).when(postImageService).deletePostImage(post);

            assertThatThrownBy(() -> service.deletePostById(post)).isSameAs(boom);

            verify(postRepository, never()).deletePost(anyLong());
        }
    }


    @Nested
    class IncrementPostLikesById {

        @Test
        void returnsIncrementedCount() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);
            int incrementedCount = 6;

            when(postRepository.incrementPostLikes(postId)).thenReturn(incrementedCount);

            int result = service.incrementPostLikesById(post);

            verify(postRepository).incrementPostLikes(postId);
            assertThat(result).isEqualTo(incrementedCount);
        }
    }
}
