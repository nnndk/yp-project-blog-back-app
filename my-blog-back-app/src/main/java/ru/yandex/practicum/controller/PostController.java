package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.dto.post.CreatePostDto;
import ru.yandex.practicum.dto.post.PaginatedPostListDto;
import ru.yandex.practicum.dto.post.PostDto;
import ru.yandex.practicum.dto.post.UpdatePostDto;
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
        return postProviderService.searchPostsPaginated(search, pageNumber, pageSize);
    }

    @GetMapping("/{id}")
    public PostDto getPostById(@PathVariable("id") long id) {
        return postProviderService.getPostById(id);
    }

    @PostMapping
    public PostDto createPost(@RequestBody CreatePostDto dto) {
        return postEditorService.createPost(dto);
    }

    @PutMapping
    public PostDto updatePost(@RequestBody UpdatePostDto dto) {
        return postEditorService.updatePost(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") long id) {
        postEditorService.deletePostById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/id}/likes")
    public int incrementPostLikes(@PathVariable("id") long id) {
        return postEditorService.incrementPostLikesById(id);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getPostImageById(@PathVariable("id") long id) {
        // надо проверить, что конкретно требует фронт. Правильно ли я вернул или надо именно byte[]?
        Resource image = postImageService.getPostImageById(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(image);
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Void> updatePostImage(
            @PathVariable("id") long id,
            @RequestParam("image") MultipartFile image
    ) {
        postImageService.updatePostImageById(id, image);
        return ResponseEntity.ok().build();
    }
}
