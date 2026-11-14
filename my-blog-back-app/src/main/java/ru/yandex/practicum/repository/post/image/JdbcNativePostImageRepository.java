package ru.yandex.practicum.repository.post.image;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.post.image.CreatePostImageDto;
import ru.yandex.practicum.dto.post.image.UpdatePostImageDto;
import ru.yandex.practicum.model.post.PostImage;
import ru.yandex.practicum.tools.DateTimeTools;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcNativePostImageRepository implements PostImageRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeTools dateTimeTools;


    @Override
    public Optional<PostImage> findPostImageByPostId(long postId) {
        return jdbcTemplate.query("""
            select post_id, origin_filename, storage_file_id from post_images
            where post_id = ?
            """,
                (rs, rowNum) -> new PostImage(
                        rs.getLong("post_id"),
                        rs.getString("origin_filename"),
                        rs.getString("storage_file_id")
                ),
                postId
        ).stream().findFirst();
    }

    @Override
    public PostImage createPostImage(long postId, CreatePostImageDto dto) {
        OffsetDateTime now = dateTimeTools.getCurrentDateTime();

        jdbcTemplate.update(
                "insert into post_images(post_id, origin_filename, storage_file_id, created_at, modified_at) values(?, ?, ?, ?, ?)",
                postId,
                dto.originFileName(),
                dto.storageFileId(),
                now,
                now
        );

        return findPostImageByPostId(postId).orElseThrow(
                () -> new RuntimeException("Failed to get created post image"));
    }

    @Override
    public void updatePostImage(long postId, UpdatePostImageDto dto) {
        OffsetDateTime now = dateTimeTools.getCurrentDateTime();

        jdbcTemplate.update(
                "update post_images set origin_filename = ?, storage_file_id = ?, modified_at = ? where post_id = ?",
                dto.originFileName(),
                dto.storageFileId(),
                now,
                postId
        );
    }

    @Override
    public void deletePostImage(long postId) {
        jdbcTemplate.update(
                "delete from post_images where post_id = ?",
                postId
        );
    }
}
