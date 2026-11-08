package ru.yandex.practicum.service.post.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.model.common.tools.ArrayDiff;
import ru.yandex.practicum.repository.post.tag.TagRepository;
import ru.yandex.practicum.tools.ArrayDiffTools;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final ArrayDiffTools arrayDiffTools;


    @Override
    public List<String> getPostTags(Post post) {
        return tagRepository.getPostTags(post.getId());
    }

    @Override
    public Map<Long, List<String>> getPostTags(List<Post> posts) {
        return tagRepository.getPostTags(posts.stream().mapToLong(Post::getId).toArray());
    }

    @Override
    public void addTagsToPost(Post post, String[] tags) {
        if (tags.length > 0) {
            tagRepository.addTagsToPost(post.getId(), tags);
        }
    }

    @Override
    public void deleteTagsFromPost(Post post, String[] tags) {
        if (tags.length > 0) {
            tagRepository.deleteTagsFromPost(post.getId(), tags);
        }
    }

    @Override
    public void updatePostTags(Post post, String[] tags) {
        String[] currentTags = getPostTags(post).toArray(String[]::new);

        if (currentTags.length == 0) {
            addTagsToPost(post, tags);
        } else {
            ArrayDiff tagDiff = arrayDiffTools.getArrayDiff(currentTags, tags);
            addTagsToPost(post, tagDiff.itemsToAdd().toArray(String[]::new));
            deleteTagsFromPost(post, tagDiff.itemsToDelete().toArray(String[]::new));
        }
    }
}
