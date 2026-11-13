package ru.yandex.practicum.service.post.image;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.dto.post.image.CreatePostImageDto;
import ru.yandex.practicum.dto.post.image.UpdatePostImageDto;
import ru.yandex.practicum.exceptions.post.image.PostImageNotFountException;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.model.post.PostImage;
import ru.yandex.practicum.repository.post.image.PostImageRepository;
import ru.yandex.practicum.service.storage.FileStorageService;

@Service
public class PostImageServiceImpl implements PostImageService {

    private final FileStorageService fileStorageService;
    private final PostImageRepository postImageRepository;

    public PostImageServiceImpl(
            @Qualifier("postImageFileStorageService") FileStorageService fileStorageService,
            PostImageRepository postImageRepository
    ) {
        this.fileStorageService = fileStorageService;
        this.postImageRepository = postImageRepository;
    }


    @Override
    public Resource getPostImage(Post post) {
        PostImage postImage = postImageRepository.findPostImageByPostId(post.getId()).orElse(null);

        if (postImage != null) {
            Resource postImageFile = fileStorageService.getFile(postImage.getStorageFileId()).orElse(null);
            if (postImageFile != null) {
                return postImageFile;
            }
        }

        return new ByteArrayResource(new byte[0]);
    }

    @Override
    public void updatePostImage(Post post, MultipartFile imageFile) {
        PostImage originPostImage = postImageRepository.findPostImageByPostId(post.getId()).orElse(null);
        boolean firstPostImage = originPostImage == null;
        String fileStorageId = fileStorageService.saveFile(imageFile);

        try {
            if (firstPostImage) {
                postImageRepository.createPostImage(
                        post.getId(),
                        new CreatePostImageDto(imageFile.getOriginalFilename(), fileStorageId)
                );
            } else {
                postImageRepository.updatePostImage(
                        post.getId(),
                        new UpdatePostImageDto(imageFile.getOriginalFilename(), fileStorageId)
                );
            }
        } catch (Exception e) {
            fileStorageService.deleteFile(fileStorageId);
            throw e;
        }

        if (!firstPostImage) {
            fileStorageService.deleteFile(originPostImage.getStorageFileId());
        }
    }

    @Override
    public void deletePostImage(Post post) {
        PostImage postImage = postImageRepository.findPostImageByPostId(post.getId()).orElseThrow(
                () -> new PostImageNotFountException(post.getId()));

        fileStorageService.deleteFile(postImage.getStorageFileId());
        postImageRepository.deletePostImage(postImage.getPostId());
    }
}
