package ru.yandex.practicum.exceptions.post.comment;

import ru.yandex.practicum.exceptions.base.ItemNotFoundException;

public class CommentNotFoundException extends ItemNotFoundException {

    public CommentNotFoundException(long postId, long commentId) {
        super(String.format("Comment with id = `%d` for post with `%d` not found", commentId, postId));
    }
}
