package ru.yandex.practicum.dto.post;

public record UpdatePostWithTagsDto(
        long id,
        String title,
        String text,
        String[] tags
) { }
