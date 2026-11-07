package ru.yandex.practicum.service.post.comment.editor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;
import ru.yandex.practicum.exceptions.post.comment.CommentNotFountException;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.provider.PostProviderService;

@Service
@RequiredArgsConstructor
public class CommentEditorServiceImpl implements CommentEditorService {

    private final CommentRepository commentRepository;


    @Override
    public CommentDto createPostComment(Post post, CreateCommentDto dto) {
        return commentRepository.createPostComment(post.getId(), dto);
    }

    @Override
    public CommentDto updatePostComment(Post post, Comment comment, UpdateCommentDto dto) {
        return commentRepository.updatePostComment(post.getId(), comment.getId(), dto);
    }

    @Override
    public void deletePostCommentById(Post post, Comment comment) {
        commentRepository.deleteCommentByPostIdByCommentId(post.getId(), comment.getId());
    }
}
