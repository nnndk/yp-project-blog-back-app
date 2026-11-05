package ru.yandex.practicum.service.post.image;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface PostImageService {

    Resource getPostImageById(long id);

    void updatePostImageById(long id, MultipartFile imageFile);
}
