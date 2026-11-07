package ru.yandex.practicum.repository.post;

import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;
import ru.yandex.practicum.dto.post.UpdatePostWithTagsDto;
import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PostRepository {

    Optional<Post> findPostById(long id);

    long countSearchedPosts(String searchText);

    List<Post> searchPostsByTextPaginated(String searchText, long offset, int limit);

    Post createPost(CreatePostDto dto);

    void updatePost(long id, UpdatePostDto dto);

    void deletePost(long id);

    int incrementPostLikes(long id);
}
