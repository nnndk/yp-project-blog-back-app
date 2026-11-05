package ru.yandex.practicum.dto.post.comment;

public record CommentDto(
        long id,
        String text,
        long postId
) { }
