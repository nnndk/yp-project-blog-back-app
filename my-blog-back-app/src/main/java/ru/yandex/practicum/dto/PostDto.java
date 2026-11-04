package ru.yandex.practicum.dto;

public record PostDto(
    Long id,
    String title,
    String text,
    String[] tags,
    int likesCount,
    int commentsCount
) { }
