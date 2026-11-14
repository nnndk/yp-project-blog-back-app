package ru.yandex.practicum.repository.post.tag;

import java.util.List;
import java.util.Map;

public interface TagRepository {

    List<String> getPostTags(long postId);

    Map<Long, List<String>> getPostTags(long[] postIds);

    void addTagsToPost(long id, String[] tags);

    void deleteTagsFromPost(long id, String[] tags);
}
