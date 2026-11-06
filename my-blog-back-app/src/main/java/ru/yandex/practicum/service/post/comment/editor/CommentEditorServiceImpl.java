package ru.yandex.practicum.service.post.comment.editor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;
import ru.yandex.practicum.exceptions.post.PostNotFountException;
import ru.yandex.practicum.exceptions.post.comment.CommentNotFountException;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentEditorServiceImpl implements CommentEditorService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;


    @Override
    public CommentDto createPostComment(long postId, CreateCommentDto dto) {
        ensurePostExist(postId);
        return commentRepository.createPostComment(postId, dto);
    }

    @Override
    public CommentDto updatePostComment(long postId, long commentId, UpdateCommentDto dto) {
        ensurePostAndCommentExist(postId, commentId);
        return commentRepository.updatePostComment(postId, commentId, dto);
    }

    @Override
    public void deletePostCommentById(long postId, long commentId) {
        ensurePostAndCommentExist(postId, commentId);
        commentRepository.deleteCommentByPostIdByCommentId(postId, commentId);
    }


    private void ensurePostAndCommentExist(long postId, long commentId) {
        ensurePostExist(postId);
        ensureCommentExist(postId, commentId);
    }

    private void ensurePostExist(long postId) {
        if (!postRepository.postWithIdExist(postId)) {
            throw new PostNotFountException(postId);
        }
    }

    private void ensureCommentExist(long postId, long commentId) {
        if (!commentRepository.commentWithIdInPostExist(postId, commentId)) {
            throw new CommentNotFountException(postId, commentId);
        }
    }
}
