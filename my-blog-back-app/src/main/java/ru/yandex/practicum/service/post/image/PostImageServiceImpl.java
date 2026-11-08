package ru.yandex.practicum.service.post.image;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.model.Post;

@Service
@RequiredArgsConstructor
public class PostImageServiceImpl implements PostImageService {


    @Override
    public Resource getPostImage(Post post) {
        return null;
    }

    @Override
    public void updatePostImage(Post post, MultipartFile imageFile) {

    }
}
