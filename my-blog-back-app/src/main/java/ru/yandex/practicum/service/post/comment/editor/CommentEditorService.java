package ru.yandex.practicum.service.post.comment.editor;

import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;

public interface CommentEditorService {

    CommentDto createPostComment(long postId, CreateCommentDto dto);

    CommentDto updatePostComment(long postId, long commentId, UpdateCommentDto dto);

    void deletePostCommentById(long postId, long commentId);
}
