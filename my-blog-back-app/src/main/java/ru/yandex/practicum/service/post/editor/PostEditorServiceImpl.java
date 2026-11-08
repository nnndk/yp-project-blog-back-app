package ru.yandex.practicum.service.post.editor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.*;
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
        List<String> tags = tagService.getPostTags(post);
        int commentsCount = commentRepository.countPostComments(post.getId());

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
    public PostDto updatePost(Post post, UpdatePostWithTagsDto dto) {
        UpdatePostDto postDto = new UpdatePostDto(dto.id(), dto.title(), dto.text());

        postRepository.updatePost(post.getId(), postDto);
        tagService.updatePostTags(post, dto.tags());

        return postProviderService.getRichPostById(post);
    }

    @Override
    public void deletePostById(Post post) {
        postImageService.deletePostImage(post);
        postRepository.deletePost(post.getId()); // комментарии и связи с тегами поста удаляются каскадно
    }

    @Override
    public int incrementPostLikesById(Post post) {
        return postRepository.incrementPostLikes(post.getId());
    }
}
