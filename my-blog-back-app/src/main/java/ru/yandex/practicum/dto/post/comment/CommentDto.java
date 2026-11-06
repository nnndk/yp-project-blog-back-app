package ru.yandex.practicum.dto.post.comment;

import ru.yandex.practicum.model.Comment;

public record CommentDto(
        long id,
        String text,
        long postId
) {
    public static CommentDto fromModel(Comment model) {
        return new CommentDto(
                model.getId(),
                model.getText(),
                model.getPostId()
        );
    }
}
