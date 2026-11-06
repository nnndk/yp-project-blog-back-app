package ru.yandex.practicum.exceptions.post;

import ru.yandex.practicum.exceptions.base.ItemNotFoundException;

public class PostNotFountException extends ItemNotFoundException {

    public PostNotFountException(long id) {
        super(String.format("Post with id = `%d` not found", id));
    }
}
