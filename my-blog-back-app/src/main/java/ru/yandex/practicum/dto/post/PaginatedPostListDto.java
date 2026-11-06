package ru.yandex.practicum.dto.post;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.model.common.PaginatedList;

import java.util.List;

@Getter
@Setter
public class PaginatedPostListDto extends PaginatedList {

    private List<PostDto> posts;


    public PaginatedPostListDto(boolean hasPrev, boolean hasNext, int lastPage, List<PostDto> posts) {
        super(hasPrev, hasNext, lastPage);
        this.posts = posts;
    }
}
