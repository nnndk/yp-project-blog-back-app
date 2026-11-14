package ru.yandex.practicum.dto.post;

import ru.yandex.practicum.model.post.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record PostDto(
    Long id,
    String title,
    String text,
    String[] tags,
    int likesCount,
    int commentsCount
) {
    public static PostDto fromModel(
            Post model,
            List<String> tags,
            int commentsCount
    ) {
        return new PostDto(
                model.getId(),
                model.getTitle(),
                model.getText().substring(0, Math.min(128, model.getText().length())),
                tags.toArray(String[]::new),
                model.getLikesCount(),
                commentsCount
        );
    }

    public static List<PostDto> fromModelList(
            List<Post> modelList,
            Map<Long, List<String>> tags,
            Map<Long, Integer> commentsCount
    ) {
        if (modelList == null || modelList.isEmpty()) {
            return new ArrayList<>();
        }

        List<PostDto> postsDto = new ArrayList<>();

        for (Post post : modelList) {
            List<String> postTags = tags.computeIfAbsent(post.getId(), x -> new ArrayList<>());
            int postCommentsCount = commentsCount.computeIfAbsent(post.getId(), x -> 0);

            PostDto postDto = PostDto.fromModel(post, postTags, postCommentsCount);
            postsDto.add(postDto);
        }

        return postsDto;
    }
}
