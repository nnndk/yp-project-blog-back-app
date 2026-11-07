package ru.yandex.practicum.service.post.comment.provider;

import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.Post;

import java.util.List;

public interface CommentProviderService {

    List<CommentDto> getPostComments(Post post);

    CommentDto getPostCommentDtoById(Post post, long commentId);

    Comment getPostCommentById(Post post, long commentId);
}