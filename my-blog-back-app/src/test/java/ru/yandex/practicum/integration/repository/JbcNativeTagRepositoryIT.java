package ru.yandex.practicum.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.integration.configuration.RepositoryTestConfig;
import ru.yandex.practicum.repository.post.tag.TagRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(RepositoryTestConfig.class)
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class JbcNativeTagRepositoryIT {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        insertPost(1L);
        insertPost(2L);
        insertPost(3L);
    }


    private void insertPost(long id) {
        jdbcTemplate.update("insert into posts(id, title, text) values (?, ?, ?)",
                id, "title" + id, "text" + id);
    }

    private long insertTag(String tag) {
        jdbcTemplate.update("insert into tags(tag) values (?)", tag);
        return jdbcTemplate.queryForObject(
                "select id from tags where tag = ?", Long.class, tag);
    }

    private void link(long postId, long tagId) {
        jdbcTemplate.update(
                "insert into posts_tags_link(post_id, tag_id) values (?, ?)",
                postId, tagId);
    }

    private long countLinks(long postId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from posts_tags_link where post_id = ?",
                Long.class, postId);
    }

    private long countTags() {
        return jdbcTemplate.queryForObject("select count(*) from tags", Long.class);
    }

    private boolean tagExists(String tag) {
        return jdbcTemplate.queryForObject(
                "select count(*) from tags where tag = ?", Long.class, tag) > 0;
    }


    @Nested
    class GetPostTagsSingle {

        @Test
        void noTags_returnsEmptyList() {
            assertThat(tagRepository.getPostTags(1L)).isEmpty();
        }

        @Test
        void returnsTagsOfGivenPost() {
            long t1 = insertTag("tag_1");
            long t2 = insertTag("tag_2");
            link(1L, t1);
            link(1L, t2);
            link(2L, t1);

            assertThat(tagRepository.getPostTags(1L))
                    .containsExactlyInAnyOrder("tag_1", "tag_2");
        }

        @Test
        void orderedByTagName() {
            long t1 = insertTag("zebra");
            long t2 = insertTag("apple");
            long t3 = insertTag("mango");
            link(1L, t1);
            link(1L, t2);
            link(1L, t3);

            assertThat(tagRepository.getPostTags(1L))
                    .containsExactly("apple", "mango", "zebra");
        }
    }


    @Nested
    class GetPostTagsBatch {

        @Test
        void emptyArray_returnsEmptyMap() {
            assertThat(tagRepository.getPostTags(new long[0])).isEmpty();
        }

        @Test
        void nullArray_returnsEmptyMap() {
            assertThat(tagRepository.getPostTags(null)).isEmpty();
        }

        @Test
        void returnsTagsForAllRequestedPosts() {
            long t1 = insertTag("tag_1");
            long t2 = insertTag("tag_2");
            link(1L, t1);
            link(2L, t2);

            Map<Long, List<String>> result = tagRepository.getPostTags(new long[]{1L, 2L});

            assertThat(result).containsEntry(1L, List.of("tag_1"));
            assertThat(result).containsEntry(2L, List.of("tag_2"));
            assertThat(result).hasSize(2);
        }

        @Test
        void postsWithoutTags_absentFromMap() {
            long t1 = insertTag("tag_1");
            link(1L, t1);

            Map<Long, List<String>> result = tagRepository.getPostTags(new long[]{1L, 2L});

            assertThat(result).containsEntry(1L, List.of("tag_1"));
            assertThat(result).doesNotContainKey(2L);
        }

        @Test
        void tagsWithinPostOrderedByName() {
            long t1 = insertTag("zebra");
            long t2 = insertTag("apple");
            long t3 = insertTag("mango");
            link(1L, t1);
            link(1L, t2);
            link(1L, t3);

            Map<Long, List<String>> result = tagRepository.getPostTags(new long[]{1L});

            assertThat(result.get(1L)).containsExactly("apple", "mango", "zebra");
        }
    }


    @Nested
    class AddTagsToPost {

        @Test
        void createsNewTagsAndLinks() {
            tagRepository.addTagsToPost(1L, new String[]{"tag_1", "tag_2"});

            assertThat(tagExists("tag_1")).isTrue();
            assertThat(tagExists("tag_2")).isTrue();
            assertThat(countLinks(1L)).isEqualTo(2);
            assertThat(tagRepository.getPostTags(1L))
                    .containsExactlyInAnyOrder("tag_1", "tag_2");
        }

        @Test
        void mixedExistingAndNew() {
            insertTag("tag_1");
            tagRepository.addTagsToPost(1L, new String[]{"tag_1", "tag_2", "tag_3"});

            assertThat(countTags()).isEqualTo(3);
            assertThat(tagRepository.getPostTags(1L))
                    .containsExactlyInAnyOrder("tag_1", "tag_2", "tag_3");
        }

        @Test
        void existingTags_onlyLinks_doesNotDuplicate() {
            insertTag("tag_1");
            tagRepository.addTagsToPost(1L, new String[]{"tag_1"});

            assertThat(countTags()).isEqualTo(1);
            assertThat(tagRepository.getPostTags(1L)).containsExactly("tag_1");
            assertThat(countLinks(1L)).isEqualTo(1);
        }

        @Test
        void linksToCorrectPost() {
            tagRepository.addTagsToPost(1L, new String[]{"tag_1"});
            tagRepository.addTagsToPost(2L, new String[]{"tag_2"});

            assertThat(tagRepository.getPostTags(1L)).containsExactly("tag_1");
            assertThat(tagRepository.getPostTags(2L)).containsExactly("tag_2");
            assertThat(countLinks(3L)).isZero();
        }
    }


    @Nested
    class DeleteTagsFromPost {

        @Test
        void removesLink() {
            long t1 = insertTag("tag_1");
            long t2 = insertTag("tag_2");
            link(1L, t1);
            link(1L, t2);

            tagRepository.deleteTagsFromPost(1L, new String[]{"tag_1"});
            assertThat(tagRepository.getPostTags(1L)).containsExactly("tag_2");
        }

        @Test
        void removesUnusedTag() {
            long t1 = insertTag("tag_1");
            link(1L, t1);

            tagRepository.deleteTagsFromPost(1L, new String[]{"tag_1"});
            assertThat(tagExists("tag_1")).isFalse();
        }

        @Test
        void keepsTagUsedByOtherPost() {
            long t1 = insertTag("tag_1");
            link(1L, t1);
            link(2L, t1);

            tagRepository.deleteTagsFromPost(1L, new String[]{"tag_1"});

            assertThat(tagExists("tag_1")).isTrue();
            assertThat(tagRepository.getPostTags(2L)).containsExactly("tag_1");
        }

        @Test
        void deletesOnlyRequestedTags() {
            long t1 = insertTag("tag_1");
            long t2 = insertTag("tag_2");
            link(1L, t1);
            link(1L, t2);

            tagRepository.deleteTagsFromPost(1L, new String[]{"tag_1"});

            assertThat(tagRepository.getPostTags(1L)).containsExactly("tag_2");
            assertThat(tagExists("tag_1")).isFalse();
            assertThat(tagExists("tag_2")).isTrue();
        }
    }
}
