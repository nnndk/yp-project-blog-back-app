package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;
import ru.yandex.practicum.model.post.Comment;
import ru.yandex.practicum.model.post.Post;
import ru.yandex.practicum.service.post.comment.editor.CommentEditorService;
import ru.yandex.practicum.service.post.comment.provider.CommentProviderService;
import ru.yandex.practicum.service.post.provider.PostProviderService;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostCommentController {

    private final CommentProviderService commentProviderService;
    private final CommentEditorService commentEditorService;
    private final PostProviderService postProviderService;


    @GetMapping("/{postId}/comments")
    public List<CommentDto> getPostComments(
            @PathVariable("postId") Long postId
    ) {
        Post post = postProviderService.getPostById(postId);
        return commentProviderService.getPostComments(post);
    }

    @GetMapping("/{postId}/comments/{commentId}")
    public CommentDto getPostById(
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId
    ) {
        Post post = postProviderService.getPostById(postId);
        return commentProviderService.getPostCommentById(post, commentId);
    }

    @PostMapping("/{postId}/comments")
    public CommentDto createPostComment(
            @PathVariable("postId") long postId,
            @RequestBody CreateCommentDto dto
    ) {
        Post post = postProviderService.getPostById(postId);
        return commentEditorService.createPostComment(post, dto);
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public CommentDto updatePostComment(
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId,
            @RequestBody UpdateCommentDto dto
    ) {
        Post post = postProviderService.getPostById(postId);
        Comment comment = commentProviderService.getPostCommentModelById(post, commentId);
        return commentEditorService.updatePostComment(post, comment, dto);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId
    ) {
        Post post = postProviderService.getPostById(postId);
        Comment comment = commentProviderService.getPostCommentModelById(post, commentId);

        commentEditorService.deletePostCommentById(post, comment);
        return ResponseEntity.ok().build();
    }
}
