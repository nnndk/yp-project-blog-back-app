package ru.yandex.practicum.repository.post;

import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.PaginatedPostListDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;

import java.util.Optional;

public interface PostRepository {

    PaginatedPostListDto searchPostsByTextPaginated(String searchText, int pageNumber, int pageSize);

    Optional<PostDto> findPostById(long id);

    boolean postWithIdExist(long id);

    PostDto createPost(CreatePostDto dto);

    PostDto updatePost(long id, UpdatePostDto dto);

    void deletePost(long id);

    int incrementPostLikes(long id);
}
