package ru.yandex.practicum.repository.post;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.tools.DateTimeTools;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JbcNativePostRepository implements PostRepository {

    private final String SELECT_FROM_POST_SQL = """
            select p.id, p.title, p.text, p.likes_count
            from posts p
            """;
    private final String COUNT_POST_SQL = """
            select count(*)
            from posts p
            """;

    private final String FILTER_BY_TAGS_SQL = """
            where p.id in (
                select ptl.post_id
                from posts_tags_link ptl
                    join tags t on t.id = ptl.tag_id
                where t.tag in (%s)
                group by ptl.post_id
                having count(distinct t.tag) = ?
            )
            """;
    private final String FILTER_BY_TITLE = "p.title ilike ?";

    private final String ORDER_PAGINATION_SQL = """
            order by p.id
            limit ?
            offset ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeTools dateTimeTools;


    @Override
    public Optional<Post> findPostById(long id) {
        return jdbcTemplate.query("""
                            %s
                            where id = ?
                        """.formatted(SELECT_FROM_POST_SQL),
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count")
                ),
                id
        ).stream().findFirst();
    }

    @Override
    public long countAllPosts() {
        return Optional.ofNullable(jdbcTemplate.queryForObject(
                COUNT_POST_SQL,
                Long.class
        )).orElse(0L);
    }

    @Override
    public long countSearchedPostsByTags(List<String> tags) {
        String placeholders = String.join(", ", Collections.nCopies(tags.size(), "?"));
        String sql = """
            %s
            %s
            """.formatted(
                    COUNT_POST_SQL,
                    FILTER_BY_TAGS_SQL.formatted(placeholders)
        );

        List<Object> params = new ArrayList<>(tags.size() + 1);
        params.addAll(tags);
        params.add(tags.size());

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Long.class, params.toArray()))
                .orElse(0L);
    }

    @Override
    public long countSearchedPostsByTitleSubstring(String substring) {
        String searchTextForQuery = prepareSearchTextForQuery(substring);
        String sql = """
            %s
            where %s
            """.formatted(
                COUNT_POST_SQL,
                FILTER_BY_TITLE
        );

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql,
                Long.class,
                searchTextForQuery
        )).orElse(0L);
    }

    @Override
    public long countSearchedPostsByTagsAndTitleSubstring(List<String> tags, String substring) {
        String searchTextForQuery = prepareSearchTextForQuery(substring);
        String placeholders = String.join(", ", Collections.nCopies(tags.size(), "?"));

        String sql = """
            %s
            %s and %s
            """.formatted(
                COUNT_POST_SQL,
                FILTER_BY_TAGS_SQL.formatted(placeholders),
                FILTER_BY_TITLE
        );

        List<Object> params = new ArrayList<>(tags.size() + 1);
        params.addAll(tags);
        params.add(tags.size());
        params.add(searchTextForQuery);

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Long.class, params.toArray()))
                .orElse(0L);
    }

    @Override
    public List<Post> getAllPostsPaginated(long offset, long limit) {
        ensurePaginationParamsCorrect(offset, limit);

        String sql = """
                %s
                %s
                """.formatted(
                        SELECT_FROM_POST_SQL,
                        ORDER_PAGINATION_SQL
        );

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count")
                ),
                limit,
                offset
        );
    }

    @Override
    public List<Post> searchPostsByTagsPaginated(List<String> tags, long offset, long limit) {
        ensurePaginationParamsCorrect(offset, limit);
        String placeholders = String.join(", ", Collections.nCopies(tags.size(), "?"));

        String sql = """
                %s
                %s
                %s
                """.formatted(
                        SELECT_FROM_POST_SQL,
                        FILTER_BY_TAGS_SQL.formatted(placeholders),
                        ORDER_PAGINATION_SQL
        );

        List<Object> params = new ArrayList<>(tags.size() + 1);
        params.addAll(tags);
        params.add(tags.size());
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count")
                ),
                params.toArray()
        );
    }

    @Override
    public List<Post> searchPostsByTitleSubstringPaginated(String substring, long offset, long limit) {
        ensurePaginationParamsCorrect(offset, limit);
        String searchTextForQuery = prepareSearchTextForQuery(substring);

        String sql = """
                %s
                where %s
                %s
                """.formatted(
                        SELECT_FROM_POST_SQL,
                        FILTER_BY_TITLE,
                        ORDER_PAGINATION_SQL
        );

        List<Object> params = new ArrayList<>(3);
        params.add(searchTextForQuery);
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count")
                ),
                params.toArray()
        );
    }

    @Override
    public List<Post> searchPostsByTagsAndTitleSubstringPaginated(
            List<String> tags,
            String substring,
            long offset,
            long limit
    ) {
        ensurePaginationParamsCorrect(offset, limit);
        String placeholders = String.join(", ", Collections.nCopies(tags.size(), "?"));
        String searchTextForQuery = prepareSearchTextForQuery(substring);

        String sql = """
                %s
                %s and %s
                %s
                """.formatted(
                        SELECT_FROM_POST_SQL,
                        FILTER_BY_TAGS_SQL.formatted(placeholders),
                        FILTER_BY_TITLE,
                        ORDER_PAGINATION_SQL
        );

        List<Object> params = new ArrayList<>(tags.size() + 2);
        params.addAll(tags);
        params.add(tags.size());
        params.add(searchTextForQuery);
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count")
                ),
                params.toArray()
        );
    }

    @Override
    public Post createPost(CreatePostDto dto) {
        OffsetDateTime now = dateTimeTools.getCurrentDateTime();

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "insert into posts(title, text, created_at, modified_at) values(?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, dto.title());
            ps.setString(2, dto.text());
            ps.setObject(3, now);
            ps.setObject(4, now);
            return ps;
        }, keyHolder);

        long id = Optional.ofNullable((Long)keyHolder.getKeys().get("ID")).orElseThrow(
                () -> new RuntimeException("Failed to get generated key"));

        return findPostById(id).orElseThrow(
                () -> new RuntimeException("Failed to get created post"));
    }

    @Override
    public void updatePost(long id, UpdatePostDto dto) {
        OffsetDateTime now = dateTimeTools.getCurrentDateTime();
        jdbcTemplate.update("update posts set title = ?, text = ?, modified_at = ? where id = ?",
                dto.title(), dto.text(), now,  id);
    }

    @Override
    public void deletePost(long id) {
        // комменты и теги удалятся каскадно базой данных
        jdbcTemplate.update("delete from posts where id = ?", id);
    }

    @Override
    public int incrementPostLikes(long id) {
        OffsetDateTime now = dateTimeTools.getCurrentDateTime();
        int likesCount = getPostLikesCount(id);
        jdbcTemplate.update(
                "update posts set likes_count = likes_count + 1, modified_at = ? where id = ?",
                now,  id);

        return likesCount + 1;
    }

    private String prepareSearchTextForQuery(String searchText) {
        return "%" + searchText+ "%";
    }

    private int getPostLikesCount(long id) {
        return jdbcTemplate.query(
                        "select likes_count from posts where id = ?",
                        (rs, n) -> rs.getInt("likes_count"),
                        id
                ).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Failed to get likes_count for post with id = `%d`", id)));
    }

    private void ensurePaginationParamsCorrect(long offset, long limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("`offset` cannot be less than 0");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("`limit` cannot be less than 1");
        }
    }
}