package ru.yandex.practicum.model.common.tools;

import java.util.List;

public record SearchComponents (
        List<String> tags,
        String substring
) {
    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    public boolean hasSubstringToSearch() {
        return substring != null && !substring.isEmpty();
    }
}
