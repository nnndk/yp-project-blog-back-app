package ru.yandex.practicum.model.post;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostImage {

    private long postId;

    private String originFileName;

    private String storageFileId;
}
