package ru.yandex.practicum.service.post.provider;

import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.PaginatedPostListDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;

public interface PostProviderService {

    PaginatedPostListDto searchPostsPaginated(String search, int pageNumber, int pageSize);

    PostDto getPostById(long id);
}
