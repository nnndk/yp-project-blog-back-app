package ru.yandex.practicum.dto.post;

public record CreatePostDto(
        String title,
        String text,
        String[] tags
) { }
