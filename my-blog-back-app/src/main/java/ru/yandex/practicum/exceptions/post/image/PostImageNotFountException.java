package ru.yandex.practicum.exceptions.post.image;

import ru.yandex.practicum.exceptions.base.ItemNotFoundException;

public class PostImageNotFountException extends ItemNotFoundException {

    public PostImageNotFountException(long postId) {
        super(String.format("Image for post with id = `%d` not found", postId));
    }
}
