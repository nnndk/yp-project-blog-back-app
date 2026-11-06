package ru.yandex.practicum.service.post.image;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostImageServiceImpl implements PostImageService {
    @Override
    public Resource getPostImageById(long id) {
        return null;
    }

    @Override
    public void updatePostImageById(long id, MultipartFile imageFile) {

    }
}
