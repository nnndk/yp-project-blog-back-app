package ru.yandex.practicum.repository.post.comment;

import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;
import ru.yandex.practicum.model.post.Comment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CommentRepository {

    Optional<Comment> findCommentByPostIdByCommentId(long postId, long commentId);

    List<Comment> findAllPostComments(long postId);

    int countPostComments(long postId);

    Map<Long, Integer> countPostComments(long[] postIds);

    Comment createPostComment(long postId, CreateCommentDto dto);

    Comment updatePostComment(long postId, long commentId, UpdateCommentDto dto);

    void deleteCommentByPostIdByCommentId(long postId, long commentId);
}
