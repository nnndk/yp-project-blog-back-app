package ru.yandex.practicum.service.post.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.PaginatedPostListDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.exceptions.post.PostNotFountException;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.tag.TagService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostProviderServiceImpl implements PostProviderService {

    private final TagService tagService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;


    @Override
    public Post getPostById(long id) {
        if (id < 1) {
            throw new IllegalArgumentException("`id` cannot be less than 1");
        }

        return postRepository.findPostById(id).orElseThrow(() -> new PostNotFountException(id));
    }

    @Override
    public PaginatedPostListDto searchPostsPaginated(String search, int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("`pageNumber` cannot be less than 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("`pageSize` cannot be less than 1");
        }

        long totalRows = postRepository.countSearchedPosts(search);
        long offset = (long) (pageNumber - 1) * pageSize;
        boolean pageWillBeEmpty = (offset + 1) > totalRows;

        List<Post> posts;
        Map<Long, List<String>> tags;
        Map<Long, Integer> commentsCount;
        int lastPage = (int) ((totalRows + pageSize - 1) / pageSize);
        boolean hasPrev = pageNumber > 1 && pageNumber <= lastPage;
        boolean hasNext = pageNumber < lastPage;

        if (pageWillBeEmpty) {
            posts = new ArrayList<>();
            tags = new HashMap<>();
            commentsCount = new HashMap<>();
        } else {
            posts = postRepository.searchPostsByTextPaginated(search, offset, pageSize);
            long[] postIds = posts.stream().mapToLong(Post::getId).toArray();
            tags = tagService.getPostTags(posts);
            commentsCount = commentRepository.countPostComments(postIds);
        }

        return new PaginatedPostListDto(
                hasPrev,
                hasNext,
                lastPage,
                PostDto.fromModelList(posts, tags, commentsCount)
        );
    }

    @Override
    public PostDto getRichPostById(Post post) {
        List<String> tags = tagService.getPostTags(post);
        int commentsCount = commentRepository.countPostComments(post.getId());

        return PostDto.fromModel(post, tags, commentsCount);
    }
}
