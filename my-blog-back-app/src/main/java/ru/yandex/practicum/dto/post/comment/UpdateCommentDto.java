package ru.yandex.practicum.dto.post.comment;

public record UpdateCommentDto(
        long id,
        String text,
        long postId
) { }
