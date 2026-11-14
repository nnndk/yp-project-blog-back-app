package ru.yandex.practicum.repository.post;

import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;
import ru.yandex.practicum.model.post.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Optional<Post> findPostById(long id);

    long countAllPosts();

    long countSearchedPostsByTags(List<String> tags);

    long countSearchedPostsByTitleSubstring(String substring);

    long countSearchedPostsByTagsAndTitleSubstring(List<String> tags, String substring);

    List<Post> getAllPostsPaginated(long offset, long limit);

    List<Post> searchPostsByTagsPaginated(List<String> tags, long offset, long limit);

    List<Post> searchPostsByTitleSubstringPaginated(String substring, long offset, long limit);

    List<Post> searchPostsByTagsAndTitleSubstringPaginated(List<String> tags, String substring, long offset, long limit);

    Post createPost(CreatePostDto dto);

    void updatePost(long id, UpdatePostDto dto);

    void deletePost(long id);

    int incrementPostLikes(long id);
}
