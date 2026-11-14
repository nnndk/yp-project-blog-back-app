package ru.yandex.practicum.exceptions.file;

import ru.yandex.practicum.exceptions.base.ItemNotFoundException;

public class FileNotFountException extends ItemNotFoundException {

    public FileNotFountException(String bucket, String filename) {
        super(String.format("File not found. Bucket: `%s`. File name: `%s`", bucket, filename));
    }
}
