package ru.yandex.practicum.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public abstract class PaginatedObjectList {

    protected boolean hasPrev;

    protected boolean haseNext;

    protected int lastPage;
}
