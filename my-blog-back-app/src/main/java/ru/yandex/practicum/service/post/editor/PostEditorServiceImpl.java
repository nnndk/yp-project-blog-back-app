package ru.yandex.practicum.service.post.editor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.*;
import ru.yandex.practicum.exceptions.post.image.PostImageNotFountException;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.image.PostImageService;
import ru.yandex.practicum.service.post.provider.PostProviderService;
import ru.yandex.practicum.service.post.tag.TagService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostEditorServiceImpl implements PostEditorService {

    private final PostProviderService postProviderService;
    private final PostImageService postImageService;
    private final PostRepository postRepository;
    private final TagService tagService;
    private final CommentRepository commentRepository;


    @Override
    public PostDto createPost(CreatePostWithTagsDto dto) {
        CreatePostDto postDto = new CreatePostDto(dto.title(), dto.text());
        Post post = postRepository.createPost(postDto);

        tagService.addTagsToPost(post, dto.tags());
        int commentsCount = commentRepository.countPostComments(post.getId());

        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getText(),
                dto.tags(),
                post.getLikesCount(),
                commentsCount
        );
    }

    @Override
    public PostDto updatePost(Post post, UpdatePostWithTagsDto dto) {
        UpdatePostDto postDto = new UpdatePostDto(post.getId(), dto.title(), dto.text());

        postRepository.updatePost(post.getId(), postDto);
        tagService.updatePostTags(post, dto.tags());

        return postProviderService.getRichPostById(post);
    }

    @Override
    public void deletePostById(Post post) {
        try {
            postImageService.deletePostImage(post);
        } catch (PostImageNotFountException ignored) { }

        postRepository.deletePost(post.getId()); // комментарии и связи с тегами поста удаляются каскадно
    }

    @Override
    public int incrementPostLikesById(Post post) {
        return postRepository.incrementPostLikes(post.getId());
    }
}
