package ru.yandex.practicum.model.post;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Post {

    private long id;

    private String title;

    private String text;

    private int likesCount;
}
