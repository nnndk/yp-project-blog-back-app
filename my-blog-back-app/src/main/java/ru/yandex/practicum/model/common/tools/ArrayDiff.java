package ru.yandex.practicum.model.common.tools;

import java.util.List;

public record ArrayDiff(
        List<String> itemsToAdd,
        List<String> itemsToDelete
) { }
