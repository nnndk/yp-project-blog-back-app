package ru.yandex.practicum.service.post.editor;

import ru.yandex.practicum.dto.post.CreatePostWithTagsDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostWithTagsDto;
import ru.yandex.practicum.model.post.Post;

public interface PostEditorService {

    PostDto createPost(CreatePostWithTagsDto dto);

    PostDto updatePost(Post post, UpdatePostWithTagsDto dto);

    void deletePost(Post post);

    int incrementPostLikesById(Post post);
}
