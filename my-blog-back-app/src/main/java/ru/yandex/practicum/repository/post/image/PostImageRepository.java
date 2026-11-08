package ru.yandex.practicum.repository.post.image;

import ru.yandex.practicum.dto.post.image.CreatePostImageDto;
import ru.yandex.practicum.dto.post.image.UpdatePostImageDto;
import ru.yandex.practicum.model.post.PostImage;

import java.util.Optional;

public interface PostImageRepository {

    Optional<PostImage> findPostImageByPostId(long postId);

    PostImage createPostImage(long postId, CreatePostImageDto dto);

    void updatePostImage(long postId, UpdatePostImageDto dto);

    void deletePostImage(long postId);
}
