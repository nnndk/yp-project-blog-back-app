package ru.yandex.practicum.integration.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
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
public class PostCommentControllerIT {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        jdbcTemplate.update("insert into posts(id, title, text) values (?, ?, ?)", 66L, "title1", "text1");
        jdbcTemplate.update("insert into posts(id, title, text) values (?, ?, ?)", 67L, "title2", "text2");
    }

    private long insertComment(long postId, String text) {
        jdbcTemplate.update("insert into comments(text, post_id) values (?, ?)", text, postId);
        return jdbcTemplate.queryForObject(
                "select max(id) from comments where post_id = ?", Long.class, postId);
    }

    private long countComments() {
        return jdbcTemplate.queryForObject("select count(*) from comments", Long.class);
    }


    @Nested
    class GetPostComments {

        @Test
        void noComments_returnsEmptyArray() throws Exception {
            mockMvc.perform(get("/posts/66/comments"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void commentsExist_returnsAll() throws Exception {
            insertComment(66L, "comment text 1");
            insertComment(66L, "comment text 2");
            insertComment(67L, "comment text 3");

            mockMvc.perform(get("/posts/66/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].text").value("comment text 1"))
                    .andExpect(jsonPath("$[1].text").value("comment text 2"))
                    .andExpect(jsonPath("$[0].postId").value(66));
        }

        @Test
        void nonexistentPost_returns404() throws Exception {
            mockMvc.perform(get("/posts/999/comments"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void nonNumericPostId_returns400() throws Exception {
            mockMvc.perform(get("/posts/undefined/comments"))
                    .andExpect(status().isBadRequest());
        }
    }


    @Nested
    class GetPostCommentById {

        @Test
        void found_returnsDto() throws Exception {
            long commentId = insertComment(66L, "comment text 1");

            mockMvc.perform(get("/posts/66/comments/" + commentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(commentId))
                    .andExpect(jsonPath("$.text").value("comment text 1"))
                    .andExpect(jsonPath("$.postId").value(66));
        }

        @Test
        void commentNotFound_returns404() throws Exception {
            mockMvc.perform(get("/posts/66/comments/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void postNotFound_returns404() throws Exception {
            mockMvc.perform(get("/posts/999/comments/1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void commentOfAnotherPost_returns404() throws Exception {
            long otherComment = insertComment(67L, "comment text 3");

            mockMvc.perform(get("/posts/66/comments/" + otherComment))
                    .andExpect(status().isNotFound());
        }
    }


    @Nested
    class CreatePostComment {

        @Test
        void validBody_createsComment() throws Exception {
            String body = """
                    {"text": "comment text 1", "postId": 66}
                    """;

            mockMvc.perform(post("/posts/66/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.text").value("comment text 1"))
                    .andExpect(jsonPath("$.postId").value(66));
        }

        @Test
        void persistsInDb() throws Exception {
            String body = """
                    {"text": "comment text 1", "postId": 66}
                    """;

            mockMvc.perform(post("/posts/66/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            assertThat(countComments()).isEqualTo(1);
        }

        @Test
        void postNotFound_returns404() throws Exception {
            String body = """
                    {"text": "comment text 1", "postId": 999}
                    """;

            mockMvc.perform(post("/posts/999/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        void malformedJson_returns400() throws Exception {
            mockMvc.perform(post("/posts/66/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{not json"))
                    .andExpect(status().isBadRequest());
        }
    }


    @Nested
    class UpdatePostComment {

        @Test
        void updatesAndReturnsDto() throws Exception {
            long commentId = insertComment(66L, "old text");
            String body = """
                    {"id": %d, "text": "new text", "postId": 66}
                    """.formatted(commentId);

            mockMvc.perform(put("/posts/66/comments/" + commentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(commentId))
                    .andExpect(jsonPath("$.text").value("new text"))
                    .andExpect(jsonPath("$.postId").value(66));
        }

        @Test
        void commentNotFound_returns404() throws Exception {
            String body = """
                    {"id": 999, "text": "new text", "postId": 66}
                    """;

            mockMvc.perform(put("/posts/66/comments/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }
    }


    @Nested
    class DeletePostComment {

        @Test
        void deletesComment() throws Exception {
            long commentId = insertComment(66L, "comment text 1");

            mockMvc.perform(delete("/posts/66/comments/" + commentId))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));

            assertThat(countComments()).isZero();
        }

        @Test
        void commentNotFound_returns404() throws Exception {
            mockMvc.perform(delete("/posts/66/comments/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void postNotFound_returns404() throws Exception {
            mockMvc.perform(delete("/posts/999/comments/1"))
                    .andExpect(status().isNotFound());
        }
    }
}
