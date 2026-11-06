package ru.yandex.practicum.repository.post;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;
import ru.yandex.practicum.model.Post;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class JbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;


    @Override
    public long countSearchedPosts(String searchText) {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
                "select count(*) from posts where text ilike ?",
                Long.class,
                "%" + searchText + "%"
        )).orElse(0L);
    }

    @Override
    public List<Post> searchPostsByTextPaginated(String searchText, long offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("`offset` cannot be less than 0");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("`limit` cannot be less than 1");
        }

        return jdbcTemplate.query("""
                    select id, title, text, likes_count from posts
                    where text ilike ?
                    order by id
                    limit ?
                    offset ?
                """,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count")
                ),
                "%" + searchText + "%",
                limit,
                offset
        );
    }

    @Override
    public Map<Long, List<String>> getPostTags(long[] postIds) {
        if (postIds == null || postIds.length == 0) {
            return Map.of();
        }

        String paramPlaceholders = String.join(",", Collections.nCopies(postIds.length, "?"));
        Object[] params = Arrays.stream(postIds).boxed().toArray();
        Map<Long, List<String>> postTags = new HashMap<>();

        jdbcTemplate.query(
                String.format("""
                select ptl.post_id as post_id, t.tag as tag
                from posts_tags_link ptl
                    inner join tags t on ptl.tag_id = t.id
                where ptl.post_id in (%s)
                order by ptl.post_id, t.tag
                """, paramPlaceholders),
                rs -> {
                    Long postId = rs.getLong("post_id");
                    String tag = rs.getString("tag");

                    postTags.computeIfAbsent(postId, x -> new ArrayList<>())
                            .add(tag);
                },
                params
        );

        return postTags;
    }

    @Override
    public Optional<Post> findPostById(long id) {
        return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    select id, title, text, likes_count from posts
                    where id = ?
                """,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count")
                ),
                id
        ));
    }

    @Override
    public List<String> getPostTags(long postId) {
        List<String> postTags = new ArrayList<>();

        jdbcTemplate.query(
                """
                select t.tag as tag
                from posts_tags_link ptl
                    inner join tags t on ptl.tag_id = t.id
                where ptl.post_id = ?
                order by t.tag
                """,
                rs -> {
                    String tag = rs.getString("tag");
                    postTags.add(tag);
                },
                postId
        );

        return postTags;
    }

    @Override
    public boolean postWithIdExist(long id) {
        return false;
    }

    @Override
    public PostDto createPost(CreatePostDto dto) {
        return null;
    }

    @Override
    public PostDto updatePost(long id, UpdatePostDto dto) {
        return null;
    }

    @Override
    public void deletePost(long id) {

    }

    @Override
    public int incrementPostLikes(long id) {
        return 0;
    }
}
