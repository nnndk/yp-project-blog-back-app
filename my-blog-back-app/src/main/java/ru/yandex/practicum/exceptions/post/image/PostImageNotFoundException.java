package ru.yandex.practicum.exceptions.post.image;

import ru.yandex.practicum.exceptions.base.ItemNotFoundException;

public class PostImageNotFoundException extends ItemNotFoundException {

    public PostImageNotFoundException(long postId) {
        super(String.format("Image for post with id = `%d` not found", postId));
    }
}
