package ru.yandex.practicum.service.post.provider;

import ru.yandex.practicum.dto.post.PaginatedPostListDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.model.post.Post;

public interface PostProviderService {

    Post getPostById(long id);

    PaginatedPostListDto searchPostsPaginated(String search, int pageNumber, long pageSize);

    PostDto getRichPostById(Post post);
}
