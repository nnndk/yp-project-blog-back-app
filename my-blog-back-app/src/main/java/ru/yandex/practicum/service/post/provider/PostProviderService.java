package ru.yandex.practicum.service.post.provider;

import ru.yandex.practicum.dto.post.PaginatedPostListDto;
import ru.yandex.practicum.dto.post.PostDto;

public interface PostProviderService {

    PaginatedPostListDto searchPostsPaginated(String search, int pageNumber, int pageSize);

    PostDto getPostById(long id);

    void ensurePostExist(long postId);
}
