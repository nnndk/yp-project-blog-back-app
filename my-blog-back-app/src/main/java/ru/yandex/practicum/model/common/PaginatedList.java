package ru.yandex.practicum.model.common;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public abstract class PaginatedList {

    protected boolean hasPrev;

    protected boolean hasNext;

    protected int lastPage;
}
