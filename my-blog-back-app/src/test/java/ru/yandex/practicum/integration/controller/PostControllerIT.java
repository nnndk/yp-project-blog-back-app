package ru.yandex.practicum.integration.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practicum.integration.configuration.WebTestConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitWebConfig(WebTestConfig.class)
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PostControllerIT {

    @Autowired private WebApplicationContext wac;
    @Autowired private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }


    private long insertPost(String title) {
        jdbcTemplate.update("insert into posts(title, text) values (?, ?)", title, "text");
        return jdbcTemplate.queryForObject(
                "select max(id) from posts", Long.class);
    }

    private void attachTag(long postId, String tag) {
        jdbcTemplate.update("insert into tags(tag) values (?)", tag);
        Long tagId = jdbcTemplate.queryForObject(
                "select id from tags where tag = ?", Long.class, tag);
        jdbcTemplate.update("insert into posts_tags_link(post_id, tag_id) values (?, ?)",
                postId, tagId);
    }

    private long countPosts() {
        return jdbcTemplate.queryForObject("select count(*) from posts", Long.class);
    }

    private long countImages() {
        return jdbcTemplate.queryForObject("select count(*) from post_images", Long.class);
    }


    @Nested
    class GetPosts {

        @Test
        void emptySearch_returnsAll() throws Exception {
            insertPost("title1");
            insertPost("title2");

            mockMvc.perform(get("/posts")
                            .param("search", "")
                            .param("pageNumber", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts.length()").value(2))
                    .andExpect(jsonPath("$.lastPage").value(1))
                    .andExpect(jsonPath("$.hasPrev").value(false))
                    .andExpect(jsonPath("$.hasNext").value(false));
        }

        @Test
        void cyrillicSearch() throws Exception {
            insertPost("Название поста 1");
            insertPost("Другое");

            mockMvc.perform(get("/posts")
                            .param("search", "аз")
                            .param("pageNumber", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts.length()").value(1))
                    .andExpect(jsonPath("$.posts[0].title").value("Название поста 1"));
        }

        @Test
        void searchWithTag() throws Exception {
            long p1 = insertPost("Название 1");
            long p2 = insertPost("Название 2");
            attachTag(p1, "tag_1");
            attachTag(p2, "tag_2");

            mockMvc.perform(get("/posts")
                            .param("search", "#tag_1")
                            .param("pageNumber", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts.length()").value(1))
                    .andExpect(jsonPath("$.posts[0].tags[0]").value("tag_1"));
        }

        @Test
        void pagination_secondPage() throws Exception {
            for (int i = 1; i <= 5; i++) insertPost("title" + i);

            mockMvc.perform(get("/posts")
                            .param("search", "")
                            .param("pageNumber", "2")
                            .param("pageSize", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts.length()").value(2))
                    .andExpect(jsonPath("$.hasPrev").value(true))
                    .andExpect(jsonPath("$.hasNext").value(true))
                    .andExpect(jsonPath("$.lastPage").value(3));
        }

        @Test
        void pageBeyondTotal_returnsEmptyPosts() throws Exception {
            insertPost("title1");

            mockMvc.perform(get("/posts")
                            .param("search", "")
                            .param("pageNumber", "100")
                            .param("pageSize", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.posts.length()").value(0));
        }

        @Test
        void missingSearchParam_returns400() throws Exception {
            mockMvc.perform(get("/posts")
                            .param("pageNumber", "1")
                            .param("pageSize", "5"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void nonNumericPageNumber_returns400() throws Exception {
            mockMvc.perform(get("/posts")
                            .param("search", "")
                            .param("pageNumber", "abc")
                            .param("pageSize", "5"))
                    .andExpect(status().isBadRequest());
        }
    }


    @Nested
    class GetPostById {

        @Test
        void found_returnsRichDto() throws Exception {
            long postId = insertPost("title1");
            attachTag(postId, "tag_1");

            mockMvc.perform(get("/posts/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(postId))
                    .andExpect(jsonPath("$.title").value("title1"))
                    .andExpect(jsonPath("$.tags[0]").value("tag_1"))
                    .andExpect(jsonPath("$.commentsCount").value(0));
        }

        @Test
        void notFound_returns404() throws Exception {
            mockMvc.perform(get("/posts/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void nonNumericId_returns400() throws Exception {
            mockMvc.perform(get("/posts/undefined"))
                    .andExpect(status().isBadRequest());
        }
    }


    @Nested
    class CreatePost {

        @Test
        void validBody_createsPost() throws Exception {
            String body = """
                    {"title": "title1", "text": "text1", "tags": ["tag_1", "tag_2"]}
                    """;

            mockMvc.perform(post("/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.title").value("title1"))
                    .andExpect(jsonPath("$.text").value("text1"))
                    .andExpect(jsonPath("$.tags.length()").value(2))
                    .andExpect(jsonPath("$.likesCount").value(0));
        }

        @Test
        void persistsInDb() throws Exception {
            String body = """
                    {"title": "title1", "text": "text1", "tags": ["tag_1"]}
                    """;

            mockMvc.perform(post("/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            assertThat(countPosts()).isEqualTo(1);
            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from posts_tags_link", Long.class)).isEqualTo(1);
        }

        @Test
        void emptyTags_noLinks() throws Exception {
            String body = """
                    {"title": "title1", "text": "text1", "tags": []}
                    """;

            mockMvc.perform(post("/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tags.length()").value(0));

            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from posts_tags_link", Long.class)).isZero();
        }

        @Test
        void malformedJson_returns400() throws Exception {
            mockMvc.perform(post("/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{not json"))
                    .andExpect(status().isBadRequest());
        }
    }


    @Nested
    class UpdatePost {

        @Test
        void updatesFieldsAndTags() throws Exception {
            long postId = insertPost("old title");
            attachTag(postId, "tag_1");
            String body = """
                    {"id": %d, "title": "new title", "text": "new text", "tags": ["tag_2"]}
                    """.formatted(postId);

            mockMvc.perform(put("/posts/" + postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("new title"))
                    .andExpect(jsonPath("$.text").value("new text"))
                    .andExpect(jsonPath("$.tags[0]").value("tag_2"));
        }

        @Test
        void usesPathIdNotDtoId() throws Exception {
            long postId = insertPost("old title");
            String body = """
                    {"id": 999, "title": "new title", "text": "new text", "tags": []}
                    """;

            mockMvc.perform(put("/posts/" + postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void notFound_returns404() throws Exception {
            String body = """
                    {"id": 999, "title": "ttt", "text": "x", "tags": []}
                    """;

            mockMvc.perform(put("/posts/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }
    }


    @Nested
    class DeletePost {

        @Test
        void deletesPostAndReturns200WithEmptyBody() throws Exception {
            long postId = insertPost("title1");

            mockMvc.perform(delete("/posts/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));

            assertThat(countPosts()).isZero();
        }

        @Test
        void cascadesRelatedRows() throws Exception {
            long postId = insertPost("title1");
            attachTag(postId, "tag_1");
            jdbcTemplate.update("insert into comments(text, post_id) values (?, ?)",
                    "comment text 1", postId);

            mockMvc.perform(delete("/posts/" + postId))
                    .andExpect(status().isOk());

            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from comments", Long.class)).isZero();
            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from posts_tags_link", Long.class)).isZero();
        }

        @Test
        void notFound_returns404() throws Exception {
            mockMvc.perform(delete("/posts/999"))
                    .andExpect(status().isNotFound());
        }
    }


    @Nested
    class IncrementPostLikes {

        @Test
        void incrementsAndReturnsNewCount() throws Exception {
            long postId = insertPost("title1");

            mockMvc.perform(post("/posts/" + postId + "/likes"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("1"));

            mockMvc.perform(post("/posts/" + postId + "/likes"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("2"));
        }

        @Test
        void notFound_returns404() throws Exception {
            mockMvc.perform(post("/posts/999/likes"))
                    .andExpect(status().isNotFound());
        }
    }


    @Nested
    class GetPostImageById {

        @Test
        void noImage_returnsEmptyBody() throws Exception {
            long postId = insertPost("title1");

            mockMvc.perform(get("/posts/" + postId + "/image"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                    .andExpect(content().bytes(new byte[0]));
        }

        @Test
        void imageExists_returnsBytes() throws Exception {
            long postId = insertPost("title1");
            byte[] data = "image data".getBytes();
            jdbcTemplate.update(
                    "insert into post_images(post_id, origin_filename, storage_file_id) values (?, ?, ?)",
                    postId, "image.png", "storage-id-1");

            mockMvc.perform(get("/posts/" + postId + "/image"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
        }

        @Test
        void postNotFound_returns404() throws Exception {
            mockMvc.perform(get("/posts/999/image"))
                    .andExpect(status().isNotFound());
        }
    }


    @Nested
    class UpdatePostImage {

        @Test
        void uploadsImage() throws Exception {
            long postId = insertPost("title1");
            MockMultipartFile image = new MockMultipartFile(
                    "image", "image.png", "image/png", "data".getBytes());

            mockMvc.perform(multipart("/posts/" + postId + "/image")
                            .file(image)
                            .with(request -> { request.setMethod("PUT"); return request; }))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));

            assertThat(countImages()).isEqualTo(1);
        }

        @Test
        void postNotFound_returns404() throws Exception {
            MockMultipartFile image = new MockMultipartFile(
                    "image", "image.png", "image/png", "data".getBytes());

            mockMvc.perform(multipart("/posts/999/image")
                            .file(image)
                            .with(request -> { request.setMethod("PUT"); return request; }))
                    .andExpect(status().isNotFound());
        }

        @Test
        void missingImagePart_returns400() throws Exception {
            long postId = insertPost("title1");

            mockMvc.perform(multipart("/posts/" + postId + "/image")
                            .with(request -> { request.setMethod("PUT"); return request; }))
                    .andExpect(status().isBadRequest());
        }
    }
}
