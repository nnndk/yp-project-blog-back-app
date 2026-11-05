package ru.yandex.practicum.dto.post;

public record PostDto(
    Long id,
    String title,
    String text,
    String[] tags,
    int likesCount,
    int commentsCount
) { }
