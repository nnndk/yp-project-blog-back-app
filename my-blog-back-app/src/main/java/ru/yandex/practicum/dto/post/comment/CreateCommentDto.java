package ru.yandex.practicum.dto.post.comment;

public record CreateCommentDto(
        String text,
        long postId
) { }
