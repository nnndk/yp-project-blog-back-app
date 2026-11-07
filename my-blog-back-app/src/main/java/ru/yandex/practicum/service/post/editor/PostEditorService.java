package ru.yandex.practicum.service.post.editor;

import ru.yandex.practicum.dto.post.CreatePostWithTagsDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostWithTagsDto;

public interface PostEditorService {

    PostDto createPost(CreatePostWithTagsDto dto);

    PostDto updatePost(long id, UpdatePostWithTagsDto dto);

    void deletePostById(long id);

    int incrementPostLikesById(long id);
}
