package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.controller.common.ControllerVerifyHelper;
import ru.yandex.practicum.dto.post.comment.CommentDto;
import ru.yandex.practicum.dto.post.comment.CreateCommentDto;
import ru.yandex.practicum.dto.post.comment.UpdateCommentDto;
import ru.yandex.practicum.exceptions.post.PostNotFountException;
import ru.yandex.practicum.exceptions.post.comment.CommentNotFountException;
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

    private final ControllerVerifyHelper controllerVerifyHelper;
    private final CommentProviderService commentProviderService;
    private final CommentEditorService commentEditorService;
    private final PostProviderService postProviderService;


    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getPostComments(
            @PathVariable("postId") Long postId
    ) {
        try {
            controllerVerifyHelper.ensureIntParamPositive("postId", postId);

            Post post = postProviderService.getPostById(postId);
            List<CommentDto> comments = commentProviderService.getPostComments(post);

            return ResponseEntity.ok(comments);
        } catch (IllegalArgumentException e) {
            return controllerVerifyHelper.handleIncorrectParamException(e);
        } catch (PostNotFountException | CommentNotFountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> getPostById(
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId
    ) {
        try {
            controllerVerifyHelper.ensureIntParamPositive("postId", postId);
            controllerVerifyHelper.ensureIntParamPositive("commentId", commentId);

            Post post = postProviderService.getPostById(postId);
            CommentDto comment = commentProviderService.getPostCommentById(post, commentId);

            return ResponseEntity.ok(comment);
        } catch (IllegalArgumentException e) {
            return controllerVerifyHelper.handleIncorrectParamException(e);
        } catch (PostNotFountException | CommentNotFountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> createPostComment(
            @PathVariable("postId") long postId,
            @RequestBody CreateCommentDto dto
    ) {
        try {
            controllerVerifyHelper.ensureIntParamPositive("postId", postId);
            dto.ensureDtoCorrect();

            Post post = postProviderService.getPostById(postId);
            CommentDto comment = commentEditorService.createPostComment(post, dto);

            return ResponseEntity.ok(comment);
        } catch (IllegalArgumentException e) {
            return controllerVerifyHelper.handleIncorrectParamException(e);
        } catch (PostNotFountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> updatePostComment(
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId,
            @RequestBody UpdateCommentDto dto
    ) {
        try {
            controllerVerifyHelper.ensureIntParamPositive("postId", postId);
            controllerVerifyHelper.ensureIntParamPositive("commentId", commentId);
            dto.ensureDtoCorrect();

            Post post = postProviderService.getPostById(postId);
            Comment comment = commentProviderService.getPostCommentModelById(post, commentId);
            CommentDto commentDto = commentEditorService.updatePostComment(post, comment, dto);

            return ResponseEntity.ok(commentDto);
        } catch (IllegalArgumentException e) {
            return controllerVerifyHelper.handleIncorrectParamException(e);
        } catch (PostNotFountException | CommentNotFountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deletePost(
            @PathVariable("postId") long postId,
            @PathVariable("commentId") long commentId
    ) {
        try {
            controllerVerifyHelper.ensureIntParamPositive("postId", postId);
            controllerVerifyHelper.ensureIntParamPositive("commentId", commentId);

            Post post = postProviderService.getPostById(postId);
            Comment comment = commentProviderService.getPostCommentModelById(post, commentId);
            commentEditorService.deletePostCommentById(post, comment);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return controllerVerifyHelper.handleIncorrectParamException(e);
        } catch (PostNotFountException | CommentNotFountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
