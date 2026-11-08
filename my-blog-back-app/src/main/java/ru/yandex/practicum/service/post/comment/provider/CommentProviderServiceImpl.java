package ru.yandex.practicum.service.post.comment.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.exceptions.post.comment.CommentNotFountException;
import ru.yandex.practicum.model.post.Comment;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.repository.post.comment.CommentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentProviderServiceImpl implements CommentProviderService {

    private final CommentRepository commentRepository;


    @Override
    public Comment getPostCommentModelById(Post post, long commentId) {
        return commentRepository.findCommentByPostIdByCommentId(post.getId(), commentId)
                .orElseThrow(() -> new CommentNotFountException(post.getId(), commentId));
    }

    @Override
    public List<CommentDto> getPostComments(Post post) {
        List<Comment> commentModels =  commentRepository.findAllPostComments(post.getId());
        return CommentDto.fromModelList(commentModels);
    }

    @Override
    public CommentDto getPostCommentById(Post post, long commentId) {
        Comment commentModel = getPostCommentModelById(post, commentId);
        return CommentDto.fromModel(commentModel);
    }
}
