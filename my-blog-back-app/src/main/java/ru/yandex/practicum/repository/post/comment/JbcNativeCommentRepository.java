package ru.yandex.practicum.repository.post.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class JbcNativeCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;


    @Override
    public Map<Long, Integer> getPostCommentsCount(long[] postIds) {
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
    public int getPostCommentsCount(long postId) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
                "select count(*) from comments where post_id = ?",
                Integer.class,
                postId
        )).orElse(0);
    }

    @Override
    public List<CommentDto> findAllPostComments(long postId) {
        return List.of();
    }

    @Override
    public Optional<CommentDto> findCommentByPostIdByCommentId(long postId, long commentId) {
        return Optional.empty();
    }

    @Override
    public boolean commentWithIdInPostExist(long postId, long commentId) {
        return false;
    }

    @Override
    public CommentDto createPostComment(long postId, CreateCommentDto dto) {
        return null;
    }

    @Override
    public CommentDto updatePostComment(long postId, long commentId, UpdateCommentDto dto) {
        return null;
    }

    @Override
    public void deleteCommentByPostIdByCommentId(long postId, long commentId) {

    }
}
