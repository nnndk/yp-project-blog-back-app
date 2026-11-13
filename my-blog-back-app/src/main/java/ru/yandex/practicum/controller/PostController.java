package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.controller.common.ControllerVerifyHelper;
import ru.yandex.practicum.controller.common.ExceptionToResponseTool;
import ru.yandex.practicum.dto.post.CreatePostWithTagsDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostWithTagsDto;
import ru.yandex.practicum.exceptions.post.PostNotFountException;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.service.post.editor.PostEditorService;
import ru.yandex.practicum.service.post.image.PostImageService;
import ru.yandex.practicum.service.post.provider.PostProviderService;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final ControllerVerifyHelper controllerVerifyHelper;
    private final ExceptionToResponseTool exceptionToResponseTool;
    private final PostProviderService postProviderService;
    private final PostImageService postImageService;
    private final PostEditorService postEditorService;


    @GetMapping
    public ResponseEntity<?> getPosts(
            @RequestParam String search,
            @RequestParam int pageNumber,
            @RequestParam int pageSize
    ) {
        if (search == null) {
            search = "";
        }

        try {
            controllerVerifyHelper.ensurePaginationParamsCorrect(pageNumber, pageSize);
            return ResponseEntity.ok(postProviderService.searchPostsPaginated(search, pageNumber, pageSize));
        } catch (IllegalArgumentException e) {
            return exceptionToResponseTool.handleIncorrectParamException(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable("id") long id) {
        try {
            controllerVerifyHelper.ensureIntParamPositive("id", id);

            Post post = postProviderService.getPostById(id);
            PostDto postDto = postProviderService.getRichPostById(post);

            return ResponseEntity.ok(postDto);
        } catch (IllegalArgumentException e) {
            return exceptionToResponseTool.handleIncorrectParamException(e);
        } catch (PostNotFountException e) {
            return exceptionToResponseTool.handleNotFoundException(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody CreatePostWithTagsDto dto) {
        try {
            dto.ensureDtoCorrect();
            return ResponseEntity.ok(postEditorService.createPost(dto));
        } catch (IllegalArgumentException e) {
            return exceptionToResponseTool.handleIncorrectParamException(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable("id") long id,
            @RequestBody UpdatePostWithTagsDto dto
    ) {
        try {
            controllerVerifyHelper.ensureIntParamPositive("id", id);
            dto.ensureDtoCorrect();
            controllerVerifyHelper.ensureParamIdAndDtoIdEqual(id, dto.id());

            Post post = postProviderService.getPostById(id);
            PostDto postDto = postEditorService.updatePost(post, dto);

            return ResponseEntity.ok(postDto);
        }
        catch (IllegalArgumentException e) {
            return exceptionToResponseTool.handleIncorrectParamException(e);
        }
        catch (PostNotFountException e) {
            return exceptionToResponseTool.handleNotFoundException(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") long id) {
        try {
            controllerVerifyHelper.ensureIntParamPositive("id", id);

            Post post = postProviderService.getPostById(id);
            postEditorService.deletePost(post);

            return ResponseEntity.ok().build();
        }
        catch (IllegalArgumentException e) {
            return exceptionToResponseTool.handleIncorrectParamException(e);
        }
        catch (PostNotFountException e) {
            return exceptionToResponseTool.handleNotFoundException(e);
        }
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<?> incrementPostLikes(@PathVariable("id") long id) {
        try {
            controllerVerifyHelper.ensureIntParamPositive("id", id);

            Post post = postProviderService.getPostById(id);
            int newPostLikes = postEditorService.incrementPostLikesById(post);

            return ResponseEntity.ok(newPostLikes);
        }
        catch (IllegalArgumentException e) {
            return exceptionToResponseTool.handleIncorrectParamException(e);
        }
        catch (PostNotFountException e) {
            return exceptionToResponseTool.handleNotFoundException(e);
        }
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<?> getPostImageById(@PathVariable("id") long id) {
        try {
            controllerVerifyHelper.ensureIntParamPositive("id", id);

            Post post = postProviderService.getPostById(id);
            Resource image = postImageService.getPostImage(post);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(image);
        }
        catch (IllegalArgumentException e) {
            return exceptionToResponseTool.handleIncorrectParamException(e);
        }
        catch (PostNotFountException e) {
            return exceptionToResponseTool.handleNotFoundException(e);
        }
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<?> updatePostImage(
            @PathVariable("id") long id,
            @RequestParam("image") MultipartFile image
    ) {
        try {
            controllerVerifyHelper.ensureIntParamPositive("id", id);
            controllerVerifyHelper.ensureImageValid(image);

            Post post = postProviderService.getPostById(id);
            postImageService.updatePostImage(post, image);

            return ResponseEntity.ok().build();
        }
        catch (IllegalArgumentException e) {
            return exceptionToResponseTool.handleIncorrectParamException(e);
        }
        catch (PostNotFountException e) {
            return exceptionToResponseTool.handleNotFoundException(e);
        }
    }
}