package ru.yandex.practicum.service.post.editor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.CreatePostWithTagsDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;
import ru.yandex.practicum.exceptions.post.PostNotFountException;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostEditorServiceImpl implements PostEditorService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;


    @Override
    public PostDto createPost(CreatePostWithTagsDto dto) {
        CreatePostDto postDto = new CreatePostDto(dto.title(), dto.text());
        Post post = postRepository.createPost(postDto);

        postRepository.addTagsToPost(post, dto.tags());
        List<String> tags = postRepository.getPostTags(post.getId());
        int commentsCount = commentRepository.getPostCommentsCount(post.getId());

        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getText(),
                tags.toArray(String[]::new),
                post.getLikesCount(),
                commentsCount
        );
    }

    @Override
    public PostDto updatePost(long id, UpdatePostDto dto) {
        ensurePostExist(id);
        return postRepository.updatePost(id, dto);
    }

    @Override
    public void deletePostById(long id) {
        ensurePostExist(id);
        postRepository.deletePost(id);
    }

    @Override
    public int incrementPostLikesById(long id) {
        ensurePostExist(id);
        return postRepository.incrementPostLikes(id);
    }


    private void ensurePostExist(long postId) {
        if (!postRepository.postWithIdExist(postId)) {
            throw new PostNotFountException(postId);
        }
    }
}
