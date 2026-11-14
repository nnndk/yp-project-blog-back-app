package ru.yandex.practicum.exceptions.base;

abstract public class ItemNotFoundException extends RuntimeException {

    protected ItemNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
