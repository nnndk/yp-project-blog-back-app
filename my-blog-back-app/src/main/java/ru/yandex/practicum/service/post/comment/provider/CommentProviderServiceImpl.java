package ru.yandex.practicum.service.post.comment.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.exceptions.post.comment.CommentNotFountException;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.post.comment.CommentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentProviderServiceImpl implements CommentProviderService {

    private final CommentRepository commentRepository;


    @Override
    public List<CommentDto> getPostComments(Post post) {
        return commentRepository.findAllPostComments(post.getId());
    }

    @Override
    public CommentDto getPostCommentDtoById(Post post, long commentId) {
        return commentRepository.findCommentByPostIdByCommentId(post.getId(), commentId)
                .orElseThrow(() -> new CommentNotFountException(post.getId(), commentId));
    }

    @Override
    public Comment getPostCommentById(Post post, long commentId) {
        CommentDto dto = getPostCommentDtoById(post, commentId);
        return new Comment(
                dto.id(),
                dto.text(),
                dto.postId()
        );
    }
}
