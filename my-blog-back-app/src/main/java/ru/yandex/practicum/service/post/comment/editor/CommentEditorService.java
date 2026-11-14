package ru.yandex.practicum.service.post.comment.editor;

import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;
import ru.yandex.practicum.model.post.Comment;
import ru.yandex.practicum.model.post.Post;

public interface CommentEditorService {

    CommentDto createPostComment(Post post, CreateCommentDto dto);

    CommentDto updatePostComment(Post post, Comment comment, UpdateCommentDto dto);

    void deletePostCommentById(Post post, Comment comment);
}
