package ru.yandex.practicum.repository.post.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.tools.DateTimeTools;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JbcNativeTagRepository implements TagRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeTools dateTimeTools;


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
    public void addTagsToPost(Post post, String[] tags) {

    }

    @Override
    public void updatePostTags(long id, String[] tags) {
        OffsetDateTime now = dateTimeTools.getCurrentDateTime();



        jdbcTemplate.update("update posts set modified_at = ? where id = ?",
                now,  id);
    }
}
