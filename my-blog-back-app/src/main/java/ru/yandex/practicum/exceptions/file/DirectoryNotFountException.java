package ru.yandex.practicum.exceptions.file;

import ru.yandex.practicum.exceptions.base.ItemNotFoundException;

public class DirectoryNotFountException extends ItemNotFoundException {

    public DirectoryNotFountException(String directory, String parentDirectoryPath) {
        super(String.format("Directory `%s` in `%s` not found", directory, parentDirectoryPath));
    }
}
