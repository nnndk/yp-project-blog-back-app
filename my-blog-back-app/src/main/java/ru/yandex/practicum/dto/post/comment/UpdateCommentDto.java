package ru.yandex.practicum.dto.post.comment;

public record UpdateCommentDto(
        long id,
        String text,
        long postId
) {
    public void ensureDtoCorrect() {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("DTO: `text` cannot be null or empty");
        }
    }
}
