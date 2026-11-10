package ru.yandex.practicum.unit.service.post;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.dto.post.image.CreatePostImageDto;
import ru.yandex.practicum.dto.post.image.UpdatePostImageDto;
import ru.yandex.practicum.exceptions.post.image.PostImageNotFountException;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.model.post.PostImage;
import ru.yandex.practicum.repository.post.image.PostImageRepository;
import ru.yandex.practicum.service.post.image.PostImageServiceImpl;
import ru.yandex.practicum.service.storage.FileStorageService;
import ru.yandex.practicum.unit.configuration.PostImageServiceImplTestConfig;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(PostImageServiceImplTestConfig.class)
public class PostImageServiceImplTest {

    private static final String STORAGE_FILE_ID = "storage-file-id-1";
    private static final String ORIGIN_FILENAME = "image.png";

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private PostImageRepository postImageRepository;

    @Autowired
    private PostImageServiceImpl service;


    @Nested
    class GetPostImage {

        @Test
        void imageNotInDb_returnsEmptyResource() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);

            when(postImageRepository.findPostImageByPostId(postId)).thenReturn(Optional.empty());

            Resource result = service.getPostImage(post);

            verify(postImageRepository).findPostImageByPostId(postId);
            verifyNoInteractions(fileStorageService);
            assertThat(result).isInstanceOf(ByteArrayResource.class);
            assertThat(((ByteArrayResource) result).getByteArray()).isEmpty();
        }

        @Test
        void fileNotInStorage_returnsEmptyResource() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);
            PostImage postImage = new PostImage(postId, ORIGIN_FILENAME, STORAGE_FILE_ID);

            when(postImageRepository.findPostImageByPostId(postId)).thenReturn(Optional.of(postImage));
            when(fileStorageService.getFile(STORAGE_FILE_ID)).thenReturn(Optional.empty());

            Resource result = service.getPostImage(post);

            verify(fileStorageService).getFile(STORAGE_FILE_ID);
            assertThat(result).isInstanceOf(ByteArrayResource.class);
            assertThat(((ByteArrayResource) result).getByteArray()).isEmpty();
        }

        @Test
        void fileInStorage_returnsResource() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);
            PostImage postImage = new PostImage(postId, ORIGIN_FILENAME, STORAGE_FILE_ID);
            Resource storedResource = new ByteArrayResource("content".getBytes());

            when(postImageRepository.findPostImageByPostId(postId)).thenReturn(Optional.of(postImage));
            when(fileStorageService.getFile(STORAGE_FILE_ID)).thenReturn(Optional.of(storedResource));

            Resource result = service.getPostImage(post);

            assertThat(result).isSameAs(storedResource);
        }
    }


    @Nested
    class UpdatePostImage {

        @Test
        void firstImage_savesAndCreates() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);
            var imageFile = new MockMultipartFile("image", ORIGIN_FILENAME, "image/png", "data".getBytes());

            when(postImageRepository.findPostImageByPostId(postId)).thenReturn(Optional.empty());
            when(fileStorageService.saveFile(imageFile)).thenReturn(STORAGE_FILE_ID);

            service.updatePostImage(post, imageFile);

            verify(fileStorageService).saveFile(imageFile);
            verify(fileStorageService, never()).deleteFile(anyString());
            verify(postImageRepository).createPostImage(
                    eq(postId),
                    eq(new CreatePostImageDto(ORIGIN_FILENAME, STORAGE_FILE_ID))
            );
            verify(postImageRepository, never()).updatePostImage(anyLong(), any());
        }

        @Test
        void existingImage_savesDeletesOldAndUpdates() {
            long postId = 66L;
            String oldStorageFileId = "old-storage-file-id";
            Post post = new Post(postId, "title1", "text1", 5);
            PostImage existing = new PostImage(postId, "old.png", oldStorageFileId);
            var imageFile = new MockMultipartFile("image", ORIGIN_FILENAME, "image/png", "data".getBytes());

            when(postImageRepository.findPostImageByPostId(postId)).thenReturn(Optional.of(existing));
            when(fileStorageService.saveFile(imageFile)).thenReturn(STORAGE_FILE_ID);

            service.updatePostImage(post, imageFile);

            verify(fileStorageService).saveFile(imageFile);
            verify(fileStorageService).deleteFile(oldStorageFileId);
            verify(postImageRepository).updatePostImage(
                    eq(postId),
                    eq(new UpdatePostImageDto(ORIGIN_FILENAME, STORAGE_FILE_ID))
            );
            verify(postImageRepository, never()).createPostImage(anyLong(), any());
        }

        @Test
        void existingImage_savesBeforeDeletingOld() {
            long postId = 66L;
            String oldStorageFileId = "old-storage-file-id";
            Post post = new Post(postId, "title1", "text1", 5);
            PostImage existing = new PostImage(postId, "old.png", oldStorageFileId);
            var imageFile = new MockMultipartFile("image", ORIGIN_FILENAME, "image/png", "data".getBytes());

            when(postImageRepository.findPostImageByPostId(postId)).thenReturn(Optional.of(existing));
            when(fileStorageService.saveFile(imageFile)).thenReturn(STORAGE_FILE_ID);

            InOrder inOrder = inOrder(fileStorageService, postImageRepository);

            service.updatePostImage(post, imageFile);

            inOrder.verify(fileStorageService).saveFile(imageFile);
            inOrder.verify(fileStorageService).deleteFile(oldStorageFileId);
            inOrder.verify(postImageRepository).updatePostImage(anyLong(), any());
        }
    }


    @Nested
    class DeletePostImage {

        @Test
        void imageNotInDb_throws() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);

            when(postImageRepository.findPostImageByPostId(postId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletePostImage(post))
                    .isInstanceOf(PostImageNotFountException.class)
                    .hasMessage((new PostImageNotFountException(postId)).getMessage());

            verifyNoInteractions(fileStorageService);
            verify(postImageRepository, never()).deletePostImage(anyLong());
        }

        @Test
        void imageInDb_deletesFileAndRecord() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);
            PostImage existing = new PostImage(postId, ORIGIN_FILENAME, STORAGE_FILE_ID);

            when(postImageRepository.findPostImageByPostId(postId)).thenReturn(Optional.of(existing));

            service.deletePostImage(post);

            verify(fileStorageService).deleteFile(STORAGE_FILE_ID);
            verify(postImageRepository).deletePostImage(postId);
        }

        @Test
        void imageInDb_deletesFileBeforeRecord() {
            long postId = 66L;
            Post post = new Post(postId, "title1", "text1", 5);
            PostImage existing = new PostImage(postId, ORIGIN_FILENAME, STORAGE_FILE_ID);

            when(postImageRepository.findPostImageByPostId(postId)).thenReturn(Optional.of(existing));

            InOrder inOrder = inOrder(fileStorageService, postImageRepository);

            service.deletePostImage(post);

            inOrder.verify(fileStorageService).deleteFile(STORAGE_FILE_ID);
            inOrder.verify(postImageRepository).deletePostImage(postId);
        }
    }
}
