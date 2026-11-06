package ru.yandex.practicum.service.post.comment.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.exceptions.post.comment.CommentNotFountException;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.provider.PostProviderService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentProviderServiceImpl implements CommentProviderService {

    private final PostProviderService postProviderService;
    private final CommentRepository commentRepository;


    @Override
    public List<CommentDto> getPostComments(long postId) {
        PostDto post = postProviderService.getPostById(postId);
        return commentRepository.findAllPostComments(post);
    }

    @Override
    public CommentDto getPostCommentById(long postId, long commentId) {
        return commentRepository.findCommentByPostIdByCommentId(postId, commentId)
                .orElseThrow(() -> new CommentNotFountException(postId, commentId));
    }
}
