package ru.yandex.practicum.repository.post;

import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;
import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PostRepository {

    long countSearchedPosts(String searchText);

    List<Post> searchPostsByTextPaginated(String searchText, long offset, int limit);

    Map<Long, List<String>> getPostTags(long[] postIds);

    Optional<Post> findPostById(long id);

    List<String> getPostTags(long postId);

    boolean postWithIdExist(long id);

    Post createPost(CreatePostDto dto);

    void addTagsToPost(Post post, String[] tags);

    PostDto updatePost(long id, UpdatePostDto dto);

    void deletePost(long id);

    int incrementPostLikes(long id);
}
