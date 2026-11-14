package ru.yandex.practicum.dto.post;

public record UpdatePostWithTagsDto(
        long id,
        String title,
        String text,
        String[] tags
) {
    public void ensureDtoCorrect() {
        if (title == null || title.isEmpty() || title.length() < 3) {
            throw new IllegalArgumentException("DTO: `title` cannot be null or empty. Minimal title length is 3 symbols");
        }

        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("DTO: `text` cannot be null or empty");
        }
    }
}
