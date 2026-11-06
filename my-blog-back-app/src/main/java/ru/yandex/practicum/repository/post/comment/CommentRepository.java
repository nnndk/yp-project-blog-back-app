package ru.yandex.practicum.repository.post.comment;

import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    List<CommentDto> findAllPostComments(PostDto post);

    Optional<CommentDto> findCommentByPostIdByCommentId(long postId, long commentId);

    boolean commentWithIdInPostExist(long postId, long commentId);

    CommentDto createPostComment(long postId, CreateCommentDto dto);

    CommentDto updatePostComment(long postId, long commentId, UpdateCommentDto dto);

    void deleteCommentByPostIdByCommentId(long postId, long commentId);
}
