package ru.yandex.practicum.service.post.image;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.model.Post;

public interface PostImageService {

    Resource getPostImage(Post post);

    void updatePostImage(Post post, MultipartFile imageFile);
}