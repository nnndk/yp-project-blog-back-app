package ru.yandex.practicum.service.post.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.post.PaginatedPostListDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.exceptions.post.PostNotFountException;
import ru.yandex.practicum.model.common.tools.PostSearchMode;
import ru.yandex.practicum.model.common.tools.SearchComponents;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.repository.post.PostRepository;
import ru.yandex.practicum.repository.post.comment.CommentRepository;
import ru.yandex.practicum.service.post.tag.TagService;
import ru.yandex.practicum.tools.SearchTools;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostProviderServiceImpl implements PostProviderService {

    private final TagService tagService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final SearchTools searchTools;


    @Override
    public Post getPostById(long id) {
        if (id < 1) {
            throw new IllegalArgumentException("`id` cannot be less than 1");
        }

        return postRepository.findPostById(id).orElseThrow(() -> new PostNotFountException(id));
    }

    @Override
    public PaginatedPostListDto searchPostsPaginated(String search, int pageNumber, long pageSize) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("`pageNumber` cannot be less than 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("`pageSize` cannot be less than 1");
        }

        SearchComponents searchComponents = searchTools.decomposeSearchText(search);
        PostSearchMode searchMode = getSearchMode(searchComponents);

        long totalRows = countSearchedPosts(searchMode, searchComponents);
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
            posts = searchPostsByTextPaginated(searchMode, searchComponents, offset, pageSize);
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

    private PostSearchMode getSearchMode(SearchComponents searchComponents) {
        if (searchComponents.hasTags()) {
            if (searchComponents.hasSubstringToSearch()) {
                return PostSearchMode.TAGS_AND_TITLE_SUBSTRING;
            } else {
                return PostSearchMode.ONLY_TAGS;
            }
        } else if (searchComponents.hasSubstringToSearch()) {
            return PostSearchMode.ONLY_TITLE_SUBSTRING;
        } else {
            return PostSearchMode.NO_FILTER;
        }
    }

    private long countSearchedPosts(PostSearchMode searchMode, SearchComponents searchComponents) {
        return switch (searchMode) {
            case TAGS_AND_TITLE_SUBSTRING ->
                    postRepository.countSearchedPostsByTagsAndTitleSubstring(
                            searchComponents.tags(), searchComponents.substring());
            case ONLY_TAGS ->
                    postRepository.countSearchedPostsByTags(searchComponents.tags());
            case ONLY_TITLE_SUBSTRING ->
                    postRepository.countSearchedPostsByTitleSubstring(searchComponents.substring());
            case NO_FILTER ->
                    postRepository.countAllPosts();
        };
    }

    private List<Post> searchPostsByTextPaginated(PostSearchMode searchMode, SearchComponents searchComponents, long offset, long pageSize) {
        return switch (searchMode) {
            case TAGS_AND_TITLE_SUBSTRING ->
                    postRepository.searchPostsByTagsAndTitleSubstringPaginated(
                            searchComponents.tags(), searchComponents.substring(), offset, pageSize);
            case ONLY_TAGS ->
                    postRepository.searchPostsByTagsPaginated(searchComponents.tags(), offset, pageSize);
            case ONLY_TITLE_SUBSTRING ->
                    postRepository.searchPostsByTitleSubstringPaginated(searchComponents.substring(), offset, pageSize);
            case NO_FILTER ->
                    postRepository.getAllPostsPaginated(offset, pageSize);
        };
    }
}