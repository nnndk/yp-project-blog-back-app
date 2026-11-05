package ru.yandex.practicum.service.post.provider;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.PaginatedPostListDto;
import ru.yandex.practicum.dto.post.PostDto;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostProviderServiceImpl implements PostProviderService {

    @Override
    public PaginatedPostListDto searchPostsPaginated(String search, int pageNumber, int pageSize) {
        List<PostDto> posts = new ArrayList<>();

        /*for (int i = 1; i <= pageSize; i++) {
            PostDto post = new PostDto(
                    (long) i,
                    "Название поста " + i,
                    "Текст поста " + i,
                    new String[] { "tag_1", "tag_2" },
                    5,
                    1
            );

            posts.add(post);
        }*/

        return new PaginatedPostListDto(
                false,
                false,
                pageNumber,
                posts
        );
    }

    @Override
    public PostDto getPostById(long id) {
        return  new PostDto(
                id,
                "Название поста " + id,
                "Текст поста " + id,
                new String[] { "tag_1", "tag_2" },
                5,
                1
        );
    }
}
