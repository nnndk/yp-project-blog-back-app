package ru.yandex.practicum.service.post;

import ru.yandex.practicum.dto.PaginatedPostListDto;

public interface PostService {

    PaginatedPostListDto getPosts();
}
