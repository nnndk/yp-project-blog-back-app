package ru.yandex.practicum.unit.service.post;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.repository.post.tag.TagRepository;
import ru.yandex.practicum.service.post.tag.TagService;
import ru.yandex.practicum.unit.configuration.TagServiceImplTestConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(TagServiceImplTestConfig.class)
public class TagServiceImplTest {

    @MockitoBean
    private TagRepository tagRepository;

    @Autowired
    private TagService service;


    @Nested
    class GetPostTagsByPost {

        @Test
        void delegatesToRepositoryWithPostId() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);

            when(tagRepository.getPostTags(postId)).thenReturn(List.of("tag_1", "tag_2"));

            List<String> result = service.getPostTags(post);

            verify(tagRepository).getPostTags(postId);
            assertThat(result).containsExactly("tag_1", "tag_2");
        }

        @Test
        void postWithoutTags_returnsEmptyList() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);

            when(tagRepository.getPostTags(postId)).thenReturn(List.of());

            assertThat(service.getPostTags(post)).isEmpty();
        }
    }


    @Nested
    class GetPostTagsByList {

        @Test
        void singlePost() {
            Post post = new Post(66L, "title1", "text1", 5);
            Map<Long, List<String>> expected = new HashMap<>(Map.of(66L, List.of("tag_1")));

            when(tagRepository.getPostTags(aryEq(new long[]{66L}))).thenReturn(expected);

            Map<Long, List<String>> result = service.getPostTags(List.of(post));

            verify(tagRepository).getPostTags(aryEq(new long[]{66L}));
            assertThat(result).containsEntry(66L, List.of("tag_1"));
        }

        @Test
        void multiplePosts_extractsAllIds() {
            Post p1 = new Post(66L, "title1", "text1", 5);
            Post p2 = new Post(67L, "title2", "text2", 3);
            Map<Long, List<String>> expected = new HashMap<>(Map.of(
                    66L, List.of("tag_1"),
                    67L, List.of("tag_2")
            ));

            when(tagRepository.getPostTags(aryEq(new long[]{66L, 67L}))).thenReturn(expected);

            Map<Long, List<String>> result = service.getPostTags(List.of(p1, p2));

            verify(tagRepository).getPostTags(aryEq(new long[]{66L, 67L}));
            assertThat(result).containsOnlyKeys(66L, 67L);
        }

        @Test
        void emptyList_delegatesWithEmptyArray() {
            when(tagRepository.getPostTags(aryEq(new long[0]))).thenReturn(new HashMap<>());

            Map<Long, List<String>> result = service.getPostTags(List.of());

            verify(tagRepository).getPostTags(aryEq(new long[0]));
            assertThat(result).isEmpty();
        }
    }


    @Nested
    class AddTagsToPost {

        @Test
        void emptyTags_doesNotCallRepository() {
            Post post = new Post(66L, "title1", "text1", 5);

            service.addTagsToPost(post, new String[0]);

            verify(tagRepository, never()).addTagsToPost(anyLong(), any(String[].class));
        }

        @Test
        void nonEmptyTags_delegatesToRepository() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);
            String[] tags = {"tag_1", "tag_2"};

            service.addTagsToPost(post, tags);

            verify(tagRepository).addTagsToPost(eq(postId), aryEq(tags));
        }
    }


    @Nested
    class DeleteTagsFromPost {

        @Test
        void emptyTags_doesNotCallRepository() {
            Post post = new Post(66L, "title1", "text1", 5);

            service.deleteTagsFromPost(post, new String[0]);

            verify(tagRepository, never()).deleteTagsFromPost(anyLong(), any(String[].class));
        }

        @Test
        void nonEmptyTags_delegatesToRepository() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);
            String[] tags = {"tag_1"};

            service.deleteTagsFromPost(post, tags);

            verify(tagRepository).deleteTagsFromPost(eq(postId), aryEq(tags));
        }
    }


    @Nested
    class UpdatePostTags {

        @Test
        void noCurrentTags_addsAllNewTags() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);
            String[] newTags = {"tag_1", "tag_2"};

            when(tagRepository.getPostTags(postId)).thenReturn(List.of());

            service.updatePostTags(post, newTags);

            verify(tagRepository).addTagsToPost(eq(postId), aryEq(newTags));
            verify(tagRepository, never()).deleteTagsFromPost(anyLong(), any(String[].class));
        }

        @Test
        void noCurrentTags_noNewTags_doesNothing() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);

            when(tagRepository.getPostTags(postId)).thenReturn(List.of());

            service.updatePostTags(post, new String[0]);

            verify(tagRepository, never()).addTagsToPost(anyLong(), any(String[].class));
            verify(tagRepository, never()).deleteTagsFromPost(anyLong(), any(String[].class));
        }

        @Test
        void identicalTags_doesNothing() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);
            String[] tags = {"tag_1", "tag_2"};

            when(tagRepository.getPostTags(postId)).thenReturn(List.of("tag_1", "tag_2"));

            service.updatePostTags(post, tags);

            verify(tagRepository, never()).addTagsToPost(anyLong(), any(String[].class));
            verify(tagRepository, never()).deleteTagsFromPost(anyLong(), any(String[].class));
        }

        @Test
        void addsOnlyMissingTags() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);

            when(tagRepository.getPostTags(postId)).thenReturn(List.of("tag_1", "tag_2"));

            service.updatePostTags(post, new String[]{"tag_1", "tag_2", "tag_3"});

            ArgumentCaptor<String[]> addCaptor = ArgumentCaptor.forClass(String[].class);
            verify(tagRepository).addTagsToPost(eq(postId), addCaptor.capture());
            verify(tagRepository, never()).deleteTagsFromPost(anyLong(), any(String[].class));

            assertThat(addCaptor.getValue()).containsExactly("tag_3");
        }

        @Test
        void deletesOnlyRemovedTags() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);

            when(tagRepository.getPostTags(postId)).thenReturn(List.of("tag_1", "tag_2", "tag_3"));

            service.updatePostTags(post, new String[]{"tag_1"});

            ArgumentCaptor<String[]> deleteCaptor = ArgumentCaptor.forClass(String[].class);
            verify(tagRepository, never()).addTagsToPost(anyLong(), any(String[].class));
            verify(tagRepository).deleteTagsFromPost(eq(postId), deleteCaptor.capture());

            assertThat(deleteCaptor.getValue()).containsExactlyInAnyOrder("tag_2", "tag_3");
        }

        @Test
        void addsAndDeletes() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);

            when(tagRepository.getPostTags(postId)).thenReturn(List.of("tag_1", "tag_2"));

            service.updatePostTags(post, new String[]{"tag_2", "tag_3"});

            ArgumentCaptor<String[]> addCaptor = ArgumentCaptor.forClass(String[].class);
            ArgumentCaptor<String[]> deleteCaptor = ArgumentCaptor.forClass(String[].class);
            verify(tagRepository).addTagsToPost(eq(postId), addCaptor.capture());
            verify(tagRepository).deleteTagsFromPost(eq(postId), deleteCaptor.capture());

            assertThat(addCaptor.getValue()).containsExactly("tag_3");
            assertThat(deleteCaptor.getValue()).containsExactly("tag_1");
        }

        @Test
        void completeReplacement() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);

            when(tagRepository.getPostTags(postId)).thenReturn(List.of("tag_1", "tag_2"));

            service.updatePostTags(post, new String[]{"tag_3", "tag_4"});

            ArgumentCaptor<String[]> addCaptor = ArgumentCaptor.forClass(String[].class);
            ArgumentCaptor<String[]> deleteCaptor = ArgumentCaptor.forClass(String[].class);
            verify(tagRepository).addTagsToPost(eq(postId), addCaptor.capture());
            verify(tagRepository).deleteTagsFromPost(eq(postId), deleteCaptor.capture());

            assertThat(addCaptor.getValue()).containsExactlyInAnyOrder("tag_3", "tag_4");
            assertThat(deleteCaptor.getValue()).containsExactlyInAnyOrder("tag_1", "tag_2");
        }

        @Test
        void removesAllTags() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);

            when(tagRepository.getPostTags(postId)).thenReturn(List.of("tag_1", "tag_2"));

            service.updatePostTags(post, new String[0]);

            ArgumentCaptor<String[]> deleteCaptor = ArgumentCaptor.forClass(String[].class);
            verify(tagRepository, never()).addTagsToPost(anyLong(), any(String[].class));
            verify(tagRepository).deleteTagsFromPost(eq(postId), deleteCaptor.capture());

            assertThat(deleteCaptor.getValue()).containsExactlyInAnyOrder("tag_1", "tag_2");
        }
    }
}
