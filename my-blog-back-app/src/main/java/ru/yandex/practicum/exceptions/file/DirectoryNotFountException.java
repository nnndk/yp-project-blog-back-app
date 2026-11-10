package ru.yandex.practicum.exceptions.file;

import ru.yandex.practicum.exceptions.base.ItemNotFoundException;

public class DirectoryNotFountException extends ItemNotFoundException {

    public DirectoryNotFountException(String directory, String fullPath) {
        super(String.format("Directory `%s` not found, Full path: `%s`", directory, fullPath));
    }
}
