package ru.yandex.practicum.dto.post;

public record CreatePostWithTagsDto(
        String title,
        String text,
        String[] tags
) { }
