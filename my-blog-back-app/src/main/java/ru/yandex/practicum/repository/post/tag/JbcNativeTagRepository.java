package ru.yandex.practicum.repository.post.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Tag;
import ru.yandex.practicum.tools.ArrayDiffTools;
import ru.yandex.practicum.tools.DateTimeTools;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.LongStream;

@Service
@RequiredArgsConstructor
public class JbcNativeTagRepository implements TagRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeTools dateTimeTools;
    private final ArrayDiffTools arrayDiffTools;


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
    public void addTagsToPost(long id, String[] tags) {
        long[] tagIds = getOrCreateTags(tags);

        String values = String.join(", ", Collections.nCopies(tagIds.length, "(?, ?)"));
        List<Object> params = new ArrayList<>(tagIds.length * 2);

        for (long tagId : tagIds) {
            params.add(id);
            params.add(tagId);
        }

        jdbcTemplate.update(
                "insert into posts_tags_link(post_id, tag_id) values " + values,
                params.toArray()
        );
    }

    @Override
    public void deleteTagsFromPost(long id, String[] tags) {
        List<Tag> tagsToDeleteFromPost = getExistingTags(tags);
        deleteTagsFromPost(id, tagsToDeleteFromPost);
        deleteUnsuedTags(tagsToDeleteFromPost);
    }

    private long[] getOrCreateTags(String[] tags) {
        List<Tag> existingTags = getExistingTags(tags);
        String[] existingTagTexts = existingTags.stream().map(Tag::getTag).toArray(String[]::new);
        List<String> tagsToCreate = arrayDiffTools.getItemsNotInLatter(tags, existingTagTexts);

        long[] createdTagIds = createTags(tagsToCreate.toArray(String[]::new));
        long[] existingTagIds = existingTags.stream().mapToLong(Tag::getId).toArray();

        return LongStream.concat(
                LongStream.of(createdTagIds),
                LongStream.of(existingTagIds)
        ).toArray();
    }

    private List<Tag> getExistingTags(String[] tags) {
        String paramPlaceholders = String.join(",", Collections.nCopies(tags.length, "?"));
        Object[] params = Arrays.stream(tags).toArray();

        return jdbcTemplate.query(
                String.format("select id, tag from tags where tag in (%s)", paramPlaceholders),
                (rs, rowNum) -> new Tag(
                        rs.getLong("id"),
                        rs.getString("tag")
                ),
                params
        );
    }

    private long[] createTags(String[] tags) {
        OffsetDateTime now = dateTimeTools.getCurrentDateTime();

        String values = String.join(", ", Collections.nCopies(tags.length, "(?, ?)"));
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "insert into tags(tag, created_at) values " + values,
                    Statement.RETURN_GENERATED_KEYS
            );
            for (int i = 0; i < tags.length; i++) {
                ps.setString(i * 2 + 1, tags[i]);
                ps.setObject(i * 2 + 2, now);
            }
            return ps;
        }, keyHolder);

        return keyHolder.getKeyList().stream()
                .mapToLong(m -> ((Number) m.get("id")).longValue())
                .toArray();
    }

    private void deleteTagsFromPost(long id, List<Tag> tags) {
        String tagParamPlaceholders = String.join(",", Collections.nCopies(tags.size(), "?"));

        List<Object> params = new ArrayList<>(tags.size() + 1);
        params.add(id);
        for (Tag tag : tags) {
            params.add(tag.getId());
        }

        jdbcTemplate.update(String.format(
                "delete from posts_tags_link where post_id = ? and tag_id in (%s)",
                tagParamPlaceholders
        ), params.toArray());
    }

    private void deleteUnsuedTags(List<Tag> tags) {
        String tagParamPlaceholders = String.join(",", Collections.nCopies(tags.size(), "?"));

        List<Object> params = new ArrayList<>(tags.size());
        for (Tag tag : tags) {
            params.add(tag.getId());
        }

        jdbcTemplate.update(String.format(
                """
                delete from tags
                where id in (%s)
                  and not exists (
                      select 1 from posts_tags_link ptl where ptl.tag_id = tags.id
                  )
                """,
                tagParamPlaceholders
            ),
            params.toArray()
        );
    }
}