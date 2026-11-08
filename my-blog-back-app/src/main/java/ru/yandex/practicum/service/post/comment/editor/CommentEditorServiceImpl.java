package ru.yandex.practicum.service.post.comment.editor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;
import ru.yandex.practicum.model.post.Comment;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.repository.post.comment.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentEditorServiceImpl implements CommentEditorService {

    private final CommentRepository commentRepository;


    @Override
    public CommentDto createPostComment(Post post, CreateCommentDto dto) {
        Comment commentModel = commentRepository.createPostComment(post.getId(), dto);
        return CommentDto.fromModel(commentModel);
    }

    @Override
    public CommentDto updatePostComment(Post post, Comment comment, UpdateCommentDto dto) {
        Comment commentModel = commentRepository.updatePostComment(post.getId(), comment.getId(), dto);
        return CommentDto.fromModel(commentModel);
    }

    @Override
    public void deletePostCommentById(Post post, Comment comment) {
        commentRepository.deleteCommentByPostIdByCommentId(post.getId(), comment.getId());
    }
}
