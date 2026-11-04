package ru.yandex.practicum.service.post;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.PaginatedPostListDto;
import ru.yandex.practicum.dto.PostDto;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    @Override
    public PaginatedPostListDto getPosts() {
        List<PostDto> posts = new ArrayList<>();

        /*for (int i = 1; i <= 5; i++) {
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
                1,
                posts
        );
    }
}
