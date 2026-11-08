package ru.yandex.practicum.model.post;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Comment {

    private long id;

    private String text;

    private long postId;
}
