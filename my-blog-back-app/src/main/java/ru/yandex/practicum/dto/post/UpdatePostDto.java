package ru.yandex.practicum.dto.post;

public record UpdatePostDto(
        long id,
        String title,
        String text
) { }
