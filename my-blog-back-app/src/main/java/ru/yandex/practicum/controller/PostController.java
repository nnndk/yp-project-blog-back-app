package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.dto.post.CreatePostWithTagsDto;
import ru.yandex.practicum.dto.post.PaginatedPostListDto;
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

    private final PostProviderService postProviderService;
    private final PostImageService postImageService;
    private final PostEditorService postEditorService;


    @GetMapping
    public PaginatedPostListDto getPosts(
            @RequestParam String search,
            @RequestParam int pageNumber,
            @RequestParam int pageSize
    ) {
        if (search == null) {
            search = "";
        }

        return postProviderService.searchPostsPaginated(search, pageNumber, pageSize);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable("id") long id) {
        try {
            Post post = postProviderService.getPostById(id);
            PostDto postDto = postProviderService.getRichPostById(post);
            return ResponseEntity.ok(postDto);
        }
        catch (PostNotFountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public PostDto createPost(@RequestBody CreatePostWithTagsDto dto) {
        return postEditorService.createPost(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable("id") long id,
            @RequestBody UpdatePostWithTagsDto dto
    ) {
        try {
            Post post = postProviderService.getPostById(id);
            PostDto postDto = postEditorService.updatePost(post, dto);
            return ResponseEntity.ok(postDto);
        }
        catch (PostNotFountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") long id) {
        try {
            Post post = postProviderService.getPostById(id);
            postEditorService.deletePostById(post);

            return ResponseEntity.ok().build();
        }
        catch (PostNotFountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<?> incrementPostLikes(@PathVariable("id") long id) {
        try {
            Post post = postProviderService.getPostById(id);
            int newPostLikes = postEditorService.incrementPostLikesById(post);

            return ResponseEntity.ok(newPostLikes);
        }
        catch (PostNotFountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<?> getPostImageById(@PathVariable("id") long id) {
        try {
            Post post = postProviderService.getPostById(id);
            Resource image = postImageService.getPostImage(post);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(image);
        }
        catch (PostNotFountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<?> updatePostImage(
            @PathVariable("id") long id,
            @RequestParam("image") MultipartFile image
    ) {
        try {
            Post post = postProviderService.getPostById(id);
            postImageService.updatePostImage(post, image);
            return ResponseEntity.ok().build();
        }
        catch (PostNotFountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}