package ru.yandex.practicum.service.post.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.PaginatedPostListDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.exceptions.post.PostNotFountException;
import ru.yandex.practicum.repository.post.PostRepository;

@Service
@RequiredArgsConstructor
public class PostProviderServiceImpl implements PostProviderService {

    private final PostRepository postRepository;


    @Override
    public PaginatedPostListDto searchPostsPaginated(String search, int pageNumber, int pageSize) {
        return postRepository.searchPostsByTextPaginated(search, pageNumber, pageSize);
    }

    @Override
    public PostDto getPostById(long id) {
        return postRepository.findPostById(id)
                .orElseThrow(() -> new PostNotFountException(id));
    }
}
