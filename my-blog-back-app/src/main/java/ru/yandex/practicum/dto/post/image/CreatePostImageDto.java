package ru.yandex.practicum.dto.post.image;

public record CreatePostImageDto(
        String originFileName,
        String storageFileId
) { }
