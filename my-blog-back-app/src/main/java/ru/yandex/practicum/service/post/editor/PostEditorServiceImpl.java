package ru.yandex.practicum.service.post.editor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;
import ru.yandex.practicum.exceptions.post.PostNotFountException;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.service.post.provider.PostProviderService;

@Service
@RequiredArgsConstructor
public class PostEditorServiceImpl implements PostEditorService {

    private final PostRepository postRepository;


    @Override
    public PostDto createPost(CreatePostDto dto) {
        return postRepository.createPost(dto);
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
