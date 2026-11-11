package ru.yandex.practicum.repository.post.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;
import ru.yandex.practicum.model.post.Comment;
import ru.yandex.practicum.tools.DateTimeTools;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class JbcNativeCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeTools dateTimeTools;


    @Override
    public Optional<Comment> findCommentByPostIdByCommentId(long postId, long commentId) {
        return jdbcTemplate.query("""
                select id, text, post_id from comments
                where post_id = ? and id = ?
            """,
                (rs, rowNum) -> new Comment(
                        rs.getLong("id"),
                        rs.getString("text"),
                        rs.getLong("post_id")
                ),
                postId,
                commentId
        ).stream().findFirst();
    }

    @Override
    public List<Comment> findAllPostComments(long postId) {
        return jdbcTemplate.query("""
                    select id, text, post_id from comments
                    where post_id = ?
                    order by id
                """,
                (rs, rowNum) -> new Comment(
                        rs.getLong("id"),
                        rs.getString("text"),
                        rs.getLong("post_id")
                ),
                postId
        );
    }

    @Override
    public int countPostComments(long postId) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
                "select count(*) from comments where post_id = ?",
                Integer.class,
                postId
        )).orElse(0);
    }

    @Override
    public Map<Long, Integer> countPostComments(long[] postIds) {
        if (postIds == null || postIds.length == 0) {
            return Map.of();
        }

        String paramPlaceholders = String.join(",", Collections.nCopies(postIds.length, "?"));
        Object[] params = Arrays.stream(postIds).boxed().toArray();
        Map<Long, Integer> commentsCountByPost = new HashMap<>();

        jdbcTemplate.query(
                String.format("""
                select post_id, count(*) as comments_count
                from comments c
                where post_id in (%s)
                group by post_id
                """, paramPlaceholders),
                rs -> {
                    Long post_id = rs.getLong("post_id");
                    Integer postCommentsCount = rs.getInt("comments_count");

                    commentsCountByPost.put(post_id, postCommentsCount);
                },
                params
        );

        return commentsCountByPost;
    }

    @Override
    public Comment createPostComment(long postId, CreateCommentDto dto) {
        OffsetDateTime now = dateTimeTools.getCurrentDateTime();

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "insert into comments(text, post_id, created_at, modified_at) values(?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, dto.text());
            ps.setLong(2, dto.postId());
            ps.setObject(3, now);
            ps.setObject(4, now);
            return ps;
        }, keyHolder);

        long commentId = Optional.ofNullable((Long)keyHolder.getKeys().get("ID")).orElseThrow(
                () -> new RuntimeException("Failed to get generated key"));

        return findCommentByPostIdByCommentId(postId, commentId).orElseThrow(
                () -> new RuntimeException("Failed to get created comment"));
    }

    @Override
    public Comment updatePostComment(long postId, long commentId, UpdateCommentDto dto) {
        OffsetDateTime now = dateTimeTools.getCurrentDateTime();

        jdbcTemplate.update("update comments set text = ?, modified_at = ? where post_id = ? and id = ?",
                dto.text(), now, postId, commentId);

        return findCommentByPostIdByCommentId(postId, commentId).orElseThrow(
                () -> new RuntimeException("Failed to get updated comment"));
    }

    @Override
    public void deleteCommentByPostIdByCommentId(long postId, long commentId) {
        jdbcTemplate.update("delete from comments where post_id = ? and id = ?", postId, commentId);
    }
}
