package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;
import ru.yandex.practicum.service.post.comment.editor.CommentEditorService;
import ru.yandex.practicum.service.post.comment.provider.CommentProviderService;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostCommentController {

    private final CommentProviderService commentProviderService;
    private final CommentEditorService commentEditorService;


    @GetMapping("/{postId}/comments")
    public List<CommentDto> getPostComments(
            @PathVariable("postId") long postId
    ) {
        return commentProviderService.getPostComments(postId);
    }

    @GetMapping("/{postId}/comments/{commentId}")
    public CommentDto getPostById(
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId
    ) {
        return commentProviderService.getPostCommentById(postId, commentId);
    }

    @PostMapping("/{postId}/comments")
    public CommentDto createPostComment(
            @PathVariable("postId") long postId,
            @RequestBody CreateCommentDto dto
    ) {
        return commentEditorService.createPostComment(postId, dto);
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public CommentDto updatePostComment(
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId,
            @RequestBody UpdateCommentDto dto
    ) {
        return commentEditorService.updatePostComment(postId, commentId, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId
    ) {
        commentEditorService.deletePostCommentById(postId, commentId);
        return ResponseEntity.ok().build();
    }
}
