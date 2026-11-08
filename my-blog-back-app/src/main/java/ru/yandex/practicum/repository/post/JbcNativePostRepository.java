package ru.yandex.practicum.repository.post;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.tools.DateTimeTools;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeTools dateTimeTools;


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
    public long countSearchedPosts(String searchText) {
        String searchTextForQuery = prepareSearchTextForQuery(searchText, true);
        return Optional.ofNullable(jdbcTemplate.queryForObject("""
                            select count(*)
                            from posts p
                                left join posts_tags_link ptl
                                on p.id = ptl.post_id
                                    left join tags t
                                    on ptl.tag_id = t.id
                            where p.title like ? or p.text like ? or t.tag like ?
                        """,
                Long.class,
                searchTextForQuery,
                searchTextForQuery,
                searchTextForQuery
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

        String searchTextForQuery = prepareSearchTextForQuery(searchText, true);

        return jdbcTemplate.query("""
                    select distinct p.id, p.title, p.text, p.likes_count
                    from posts p
                        left join posts_tags_link ptl
                        on p.id = ptl.post_id
                            left join tags t
                            on ptl.tag_id = t.id
                    where p.title like ? or p.text like ? or t.tag like ?
                    order by p.id
                    limit ?
                    offset ?
                """,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count")
                ),
                searchTextForQuery,
                searchTextForQuery,
                searchTextForQuery,
                limit,
                offset
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
        jdbcTemplate.update(
                "update posts set likes_count = likes_count + 1, modified_at = ? where id = ?",
                now,  id);

        return getPostLikesCount(id);
    }

    private String prepareSearchTextForQuery(String searchText, boolean toLower) {
        if (toLower) {
            return "%" + searchText.toLowerCase() + "%";
        } else {
            return "%" + searchText+ "%";
        }
    }

    private int getPostLikesCount(long id) {
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                        "select likes_count from posts where id = ?",
                        Integer.class,
                        id
                )
            )
            .orElseThrow(
                    () -> new IllegalStateException(
                            String.format(
                                    "Failed to get likes_count for post with id = `%d`",
                                    id
                            )
                    )
            );
    }
}
