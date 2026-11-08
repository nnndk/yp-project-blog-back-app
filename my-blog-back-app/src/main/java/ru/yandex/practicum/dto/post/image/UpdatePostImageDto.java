package ru.yandex.practicum.dto.post.image;

public record UpdatePostImageDto(
        String originFileName,
        String storageFileId
) { }
