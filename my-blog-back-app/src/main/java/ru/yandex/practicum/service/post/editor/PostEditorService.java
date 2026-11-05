package ru.yandex.practicum.service.post.editor;

import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;

public interface PostEditorService {

    PostDto createPost(CreatePostDto dto);

    PostDto updatePost(UpdatePostDto dto);

    void deletePostById(long id);

    int incrementPostLikesById(long id);
}
