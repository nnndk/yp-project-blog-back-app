package ru.yandex.practicum.service.post.editor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.*;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.repository.post.tag.TagRepository;
import ru.yandex.practicum.service.post.provider.PostProviderService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostEditorServiceImpl implements PostEditorService {

    private final PostProviderService postProviderService;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;


    @Override
    public PostDto createPost(CreatePostWithTagsDto dto) {
        CreatePostDto postDto = new CreatePostDto(dto.title(), dto.text());
        Post post = postRepository.createPost(postDto);

        tagRepository.addTagsToPost(post, dto.tags());
        List<String> tags = tagRepository.getPostTags(post.getId());
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
    public PostDto updatePost(long id, UpdatePostWithTagsDto dto) {
        postProviderService.ensurePostExist(id);
        UpdatePostDto postDto = new UpdatePostDto(dto.id(), dto.title(), dto.text());

        postRepository.updatePost(id, postDto);
        tagRepository.updatePostTags(id, dto.tags());

        return postProviderService.getPostById(id);
    }

    @Override
    public void deletePostById(long id) {
        postProviderService.ensurePostExist(id);
        postRepository.deletePost(id);
    }

    @Override
    public int incrementPostLikesById(long id) {
        postProviderService.ensurePostExist(id);
        return postRepository.incrementPostLikes(id);
    }
}
