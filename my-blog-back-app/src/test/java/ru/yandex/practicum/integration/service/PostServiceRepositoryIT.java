package ru.yandex.practicum.integration.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.dto.post.CreatePostWithTagsDto;
import ru.yandex.practicum.dto.post.PaginatedPostListDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.integration.configuration.ServiceRepositoryTestConfig;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.service.post.comment.editor.CommentEditorService;
import ru.yandex.practicum.service.post.editor.PostEditorService;
import ru.yandex.practicum.service.post.image.PostImageService;
import ru.yandex.practicum.service.post.provider.PostProviderService;
import ru.yandex.practicum.service.post.tag.TagService;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(ServiceRepositoryTestConfig.class)
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PostServiceRepositoryIT {

    @Autowired private PostEditorService postEditorService;
    @Autowired private PostProviderService postProviderService;
    @Autowired private TagService tagService;
    @Autowired private PostImageService postImageService;
    @Autowired private CommentEditorService commentEditorService;
    @Autowired private JdbcTemplate jdbcTemplate;


    private long countRows(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Long.class);
    }


    @Nested
    class CreatePostWithTags {

        @Test
        void endToEnd() {
            CreatePostWithTagsDto dto = new CreatePostWithTagsDto(
                    "title1", "text1", new String[]{"tag_1", "tag_2"});

            PostDto created = postEditorService.createPost(dto);

            assertThat(created.id()).isPositive();
            assertThat(created.title()).isEqualTo("title1");
            assertThat(created.text()).isEqualTo("text1");
            assertThat(created.tags()).containsExactly("tag_1", "tag_2");
            assertThat(created.likesCount()).isZero();
            assertThat(created.commentsCount()).isZero();

            Post post = postProviderService.getPostById(created.id());
            assertThat(post.getTitle()).isEqualTo("title1");
            assertThat(tagService.getPostTags(post)).containsExactly("tag_1", "tag_2");
        }

        @Test
        void reusesExistingTags() {
            postEditorService.createPost(new CreatePostWithTagsDto("t1", "x", new String[]{"tag_1"}));
            postEditorService.createPost(new CreatePostWithTagsDto("t2", "x", new String[]{"tag_1", "tag_2"}));

            assertThat(countRows("tags")).isEqualTo(2);
            assertThat(countRows("posts_tags_link")).isEqualTo(3);
        }

        @Test
        void emptyTags_noLinks() {
            PostDto created = postEditorService.createPost(
                    new CreatePostWithTagsDto("title1", "text1", new String[0]));

            assertThat(created.tags()).isEmpty();
            assertThat(countRows("posts_tags_link")).isZero();
        }
    }


    @Nested
    class UpdatePostTags {

        @Test
        void addsAndRemovesViaDiff() {
            PostDto created = postEditorService.createPost(
                    new CreatePostWithTagsDto("title1", "text1", new String[]{"tag_1", "tag_2"}));
            Post post = postProviderService.getPostById(created.id());

            tagService.updatePostTags(post, new String[]{"tag_2", "tag_3"});

            assertThat(tagService.getPostTags(post)).containsExactly("tag_2", "tag_3");
            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from tags where tag = 'tag_1'", Long.class)).isZero();
            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from tags where tag = 'tag_3'", Long.class)).isEqualTo(1);
        }

        @Test
        void removesAllTags() {
            PostDto created = postEditorService.createPost(
                    new CreatePostWithTagsDto("title1", "text1", new String[]{"tag_1", "tag_2"}));
            Post post = postProviderService.getPostById(created.id());

            tagService.updatePostTags(post, new String[0]);

            assertThat(tagService.getPostTags(post)).isEmpty();
            assertThat(countRows("posts_tags_link")).isZero();
            assertThat(countRows("tags")).isZero();
        }

        @Test
        void sharedTagNotRemovedWhenStillUsed() {
            PostDto p1 = postEditorService.createPost(
                    new CreatePostWithTagsDto("t1", "x", new String[]{"tag_1"}));
            postEditorService.createPost(
                    new CreatePostWithTagsDto("t2", "x", new String[]{"tag_1"}));
            Post post1 = postProviderService.getPostById(p1.id());

            tagService.updatePostTags(post1, new String[0]);

            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from tags where tag = 'tag_1'", Long.class)).isEqualTo(1);
            assertThat(countRows("posts_tags_link")).isEqualTo(1);
        }
    }


    @Nested
    class DeletePostCascade {

        @Test
        void deletesPostAndAllRelatedRows() {
            PostDto created = postEditorService.createPost(
                    new CreatePostWithTagsDto("title1", "text1", new String[]{"tag_1", "tag_2"}));
            Post post = postProviderService.getPostById(created.id());

            commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 1", post.getId()));
            commentEditorService.createPostComment(post,
                    new CreateCommentDto("comment text 2", post.getId()));
            postImageService.updatePostImage(post,
                    new MockMultipartFile("image", "image.png", "image/png", "data".getBytes()));

            postEditorService.deletePost(post);

            assertThat(countRows("posts")).isZero();
            assertThat(countRows("comments")).isZero();
            assertThat(countRows("post_images")).isZero();
            assertThat(countRows("posts_tags_link")).isZero();
            assertThat(countRows("tags")).isEqualTo(2);
        }

        @Test
        void deletesPostWithoutImage() {
            PostDto created = postEditorService.createPost(
                    new CreatePostWithTagsDto("title1", "text1", new String[0]));
            Post post = postProviderService.getPostById(created.id());

            postEditorService.deletePost(post);

            assertThat(countRows("posts")).isZero();
        }

        @Test
        void doesNotTouchOtherPosts() {
            PostDto p1 = postEditorService.createPost(
                    new CreatePostWithTagsDto("t1", "x", new String[]{"tag_1"}));
            PostDto p2 = postEditorService.createPost(
                    new CreatePostWithTagsDto("t2", "x", new String[]{"tag_1"}));

            postEditorService.deletePost(postProviderService.getPostById(p1.id()));

            assertThat(postProviderService.getPostById(p2.id()).getTitle()).isEqualTo("t2");
            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from tags where tag = 'tag_1'", Long.class)).isEqualTo(1);
        }
    }


    @Nested
    class SearchWithTagsAndSubstring {

        @Test
        void countAndDataAgree() {
            postEditorService.createPost(new CreatePostWithTagsDto(
                    "Название поста 1", "x", new String[]{"tag_1"}));
            postEditorService.createPost(new CreatePostWithTagsDto(
                    "Название поста 2", "x", new String[]{"tag_1"}));
            postEditorService.createPost(new CreatePostWithTagsDto(
                    "Название поста 3", "x", new String[]{"tag_2"}));
            postEditorService.createPost(new CreatePostWithTagsDto(
                    "Другое", "x", new String[]{"tag_1"}));

            PaginatedPostListDto result = postProviderService.searchPostsPaginated(
                    "#tag_1 Название", 1, 10);

            assertThat(result.getPosts()).hasSize(2);
            assertThat(result.getPosts()).extracting(PostDto::title)
                    .containsExactlyInAnyOrder("Название поста 1", "Название поста 2");
            assertThat(result.getPosts()).allMatch(dto -> Arrays.stream(dto.tags()).toList().contains("tag_1"));
        }

        @Test
        void filtersByTagsOnly() {
            postEditorService.createPost(new CreatePostWithTagsDto("t1", "x", new String[]{"tag_1", "tag_2"}));
            postEditorService.createPost(new CreatePostWithTagsDto("t2", "x", new String[]{"tag_1"}));

            PaginatedPostListDto result = postProviderService.searchPostsPaginated(
                    "#tag_1 #tag_2", 1, 10);

            assertThat(result.getPosts()).hasSize(1);
            assertThat(result.getPosts().getFirst().title()).isEqualTo("t1");
        }

        @Test
        void returnsCommentsCount() {
            PostDto created = postEditorService.createPost(
                    new CreatePostWithTagsDto("Название 1", "x", new String[]{"tag_1"}));
            Post post = postProviderService.getPostById(created.id());
            commentEditorService.createPostComment(post,
                    new CreateCommentDto("c1", post.getId()));
            commentEditorService.createPostComment(post,
                    new CreateCommentDto("c2", post.getId()));

            PaginatedPostListDto result = postProviderService.searchPostsPaginated(
                    "#tag_1 Название", 1, 10);

            assertThat(result.getPosts()).hasSize(1);
            assertThat(result.getPosts().getFirst().commentsCount()).isEqualTo(2);
        }

        @Test
        void emptySearch_returnsAll() {
            postEditorService.createPost(new CreatePostWithTagsDto("t1", "x", new String[0]));
            postEditorService.createPost(new CreatePostWithTagsDto("t2", "x", new String[0]));

            PaginatedPostListDto result = postProviderService.searchPostsPaginated("", 1, 10);

            assertThat(result.getPosts()).hasSize(2);
        }
    }
}
