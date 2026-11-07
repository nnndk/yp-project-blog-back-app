package ru.yandex.practicum.service.post.tag;

import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Map;

public interface TagService {

    List<String> getPostTags(Post post);

    Map<Long, List<String>> getPostTags(List<Post> posts);

    void addTagsToPost(Post post, String[] tags);

    void deleteTagsFromPost(Post post, String[] tags);

    void updatePostTags(Post post, String[] tags);
}
