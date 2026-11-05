package ru.yandex.practicum.service.post.comment.provider;

import ru.yandex.practicum.dto.post.comment.CommentDto;

import java.util.List;

public interface CommentProviderService {

    List<CommentDto> getPostComments(long postId);

    CommentDto getPostCommentById(long postId, long commentId);
}
