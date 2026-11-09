package ru.yandex.practicum.unit.service.post;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.dto.post.PaginatedPostListDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.exceptions.post.PostNotFountException;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.provider.PostProviderService;
import ru.yandex.practicum.service.post.tag.TagService;
import ru.yandex.practicum.unit.configuration.PostProviderServiceImplTestConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(PostProviderServiceImplTestConfig.class)
public class PostProviderServiceImplTest {

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private PostRepository postRepository;

    @MockitoBean
    private CommentRepository commentRepository;

    @Autowired
    private PostProviderService service;


    @Nested
    class GetPostById {

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, Long.MIN_VALUE})
        void invalidId_throws(long id) {
            assertThatThrownBy(() -> service.getPostById(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("`id` cannot be less than 1");
        }

        @Test
        void notFound_throws() {
            long postId = 66L;
            when(postRepository.findPostById(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getPostById(postId))
                    .isInstanceOf(PostNotFountException.class)
                    .hasMessage((new PostNotFountException(postId)).getMessage());
        }

        @Test
        void found_returnsPost() {
            long postId = 66L;
            Post post = new Post(postId, "title", "text", 5);
            when(postRepository.findPostById(postId)).thenReturn(Optional.of(post));

            assertThat(service.getPostById(postId)).isSameAs(post);
        }
    }


    @Nested
    class SearchPostsPaginated {

        @ParameterizedTest
        @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
        void invalidPageNumber_throws(int pageNumber) {
            assertThatThrownBy(() -> service.searchPostsPaginated(null, pageNumber, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("`pageNumber` cannot be less than 1");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
        void invalidPageSize_throws(int pageSize) {
            assertThatThrownBy(() -> service.searchPostsPaginated(null, 1, pageSize))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("`pageSize` cannot be less than 1");
        }

        @Test
        void emptyPage_doesNotFetchPosts() {
            when(postRepository.countSearchedPosts("q")).thenReturn(0L);

            PaginatedPostListDto result = service.searchPostsPaginated("q", 1, 5);

            verify(postRepository, never()).searchPostsByTextPaginated(any(), anyLong(), anyInt());
            verifyNoInteractions(tagService);
            verifyNoInteractions(commentRepository);
            assertThat(result.getPosts()).isEmpty();
        }

        @Test
        void nonEmptyPage_fetchesAndMapsPost() {
            long postId = 66L;
            String title = "title1";
            String text = "text1";
            int likesCount = 5;
            int commentsCount = 3;

            Post post = new Post(postId, title, text, likesCount);

            when(postRepository.countSearchedPosts("q")).thenReturn(postId);
            when(postRepository.searchPostsByTextPaginated("q", 0L, 5))
                    .thenReturn(List.of(post));
            when(tagService.getPostTags(List.of(post)))
                    .thenReturn(new HashMap<>(Map.of(postId, List.of("tag_1", "tag_2"))));
            when(commentRepository.countPostComments(aryEq(new long[]{postId})))
                    .thenReturn(new HashMap<>(Map.of(postId, commentsCount)));

            PaginatedPostListDto result = service.searchPostsPaginated("q", 1, 5);

            verify(postRepository).searchPostsByTextPaginated("q", 0L, 5);
            assertThat(result.getPosts()).hasSize(1);

            PostDto dto = result.getPosts().getFirst();
            assertThat(dto.id()).isEqualTo(postId);
            assertThat(dto.title()).isEqualTo(title);
            assertThat(dto.text()).isEqualTo(text);
            assertThat(dto.likesCount()).isEqualTo(likesCount);
            assertThat(dto.tags()).containsExactly("tag_1", "tag_2");
            assertThat(dto.commentsCount()).isEqualTo(commentsCount);
        }

        @Test
        void postWithoutTagsAndComments_getsEmptyDefaults() {
            long postId = 66L;
            String title = "title1";
            String text = "text1";
            int likesCount = 5;

            Post post = new Post(postId, title, text, likesCount);

            when(postRepository.countSearchedPosts("q")).thenReturn(postId);
            when(postRepository.searchPostsByTextPaginated("q", 0L, 5))
                    .thenReturn(List.of(post));
            when(tagService.getPostTags(List.of(post)))
                    .thenReturn(new HashMap<>());
            when(commentRepository.countPostComments(aryEq(new long[]{postId})))
                    .thenReturn(new HashMap<>());

            PaginatedPostListDto result = service.searchPostsPaginated("q", 1, 5);

            PostDto dto = result.getPosts().getFirst();
            assertThat(dto.tags()).isEmpty();
            assertThat(dto.commentsCount()).isZero();
        }

        @ParameterizedTest
        @CsvSource({
                //  pageNumber, pageSize, totalRows, expectedLastPage, expectedHasPrev, expectedHasNext
                "1, 5,  0, 0, false, false",
                "1, 5,  1, 1, false, false",
                "1, 5,  5, 1, false, false",
                "1, 5,  6, 2, false, true",
                "2, 5,  6, 2, true,  false",
                "1, 5, 10, 2, false, true",
                "2, 5, 10, 2, true,  false",
                "3, 5, 10, 2, false, false",
                "1, 3, 10, 4, false, true",
                "4, 3, 10, 4, true,  false",
                "5, 3, 10, 4, false, false",
        })
        void checkPagination(
                int pageNumber,
                int pageSize,
                long totalRows,
                int expectedLastPage,
                boolean expectedHasPrev,
                boolean expectedHasNext
        ) {
            when(postRepository.countSearchedPosts("q")).thenReturn(totalRows);

            long offset = (long) (pageNumber - 1) * pageSize;
            if (offset < totalRows) {
                when(postRepository.searchPostsByTextPaginated("q", offset, pageSize))
                        .thenReturn(List.of());
                when(tagService.getPostTags(anyList())).thenReturn(new HashMap<>());
                when(commentRepository.countPostComments(any(long[].class)))
                        .thenReturn(new HashMap<>());
            }

            PaginatedPostListDto result = service.searchPostsPaginated("q", pageNumber, pageSize);

            assertThat(result.getLastPage()).isEqualTo(expectedLastPage);
            assertThat(result.isHasPrev()).isEqualTo(expectedHasPrev);
            assertThat(result.isHasNext()).isEqualTo(expectedHasNext);
        }
    }


    @Nested
    class GetRichPostById {

        @Test
        void returnsRichDto() {
            long postId = 66L;
            String title = "title1";
            String text = "text1";
            int likesCount = 5;
            int commentsCount = 3;

            Post post = new Post(postId, title, text, likesCount);
            when(tagService.getPostTags(post)).thenReturn(List.of("tag_1", "tag_2"));
            when(commentRepository.countPostComments(postId)).thenReturn(commentsCount);

            PostDto dto = service.getRichPostById(post);

            verify(tagService).getPostTags(post);
            verify(commentRepository).countPostComments(postId);
            assertThat(dto.id()).isEqualTo(postId);
            assertThat(dto.title()).isEqualTo(title);
            assertThat(dto.text()).isEqualTo(text);
            assertThat(dto.likesCount()).isEqualTo(likesCount);
            assertThat(dto.tags()).containsExactly("tag_1", "tag_2");
            assertThat(dto.commentsCount()).isEqualTo(commentsCount);
        }

        @Test
        void emptyTagsAndComments_returnsEmptyDefaults() {
            long postId = 66L;
            String title = "title1";
            String text = "text1";
            int likesCount = 0;
            int commentsCount = 0;

            Post post = new Post(postId, title, text, likesCount);
            when(tagService.getPostTags(post)).thenReturn(List.of());
            when(commentRepository.countPostComments(postId)).thenReturn(commentsCount);

            PostDto dto = service.getRichPostById(post);

            assertThat(dto.tags()).isEmpty();
            assertThat(dto.commentsCount()).isZero();
        }
    }
}