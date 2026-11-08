package ru.yandex.practicum.dto.post.comment;

import ru.yandex.practicum.model.Comment;

import java.util.ArrayList;
import java.util.List;

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

    public static List<CommentDto> fromModelList(
            List<Comment> modelList
    ) {
        if (modelList == null || modelList.isEmpty()) {
            return new ArrayList<>();
        }

        List<CommentDto> commentsDto = new ArrayList<>();

        for (Comment comment : modelList) {
            CommentDto commentDto = CommentDto.fromModel(comment);
            commentsDto.add(commentDto);
        }

        return commentsDto;
    }
}
