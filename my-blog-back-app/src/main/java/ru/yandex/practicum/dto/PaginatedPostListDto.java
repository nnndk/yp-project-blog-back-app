package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.model.PaginatedObjectList;

import java.util.List;

@Getter
@Setter
public class PaginatedPostListDto extends PaginatedObjectList {

    private List<PostDto> posts;


    public PaginatedPostListDto(boolean hasPrev, boolean haseNext, int lastPage, List<PostDto> posts) {
        super(hasPrev, haseNext, lastPage);
        this.posts = posts;
    }
}
