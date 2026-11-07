package ru.yandex.practicum.repository.post.tag;

import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Map;

public interface TagRepository {

    List<String> getPostTags(long postId);

    Map<Long, List<String>> getPostTags(long[] postIds);

    void addTagsToPost(Post post, String[] tags);

    void updatePostTags(long id, String[] tags);
}
