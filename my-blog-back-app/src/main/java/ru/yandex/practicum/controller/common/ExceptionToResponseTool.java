package ru.yandex.practicum.controller.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.exceptions.base.ItemNotFoundException;

@Component
public class ExceptionToResponseTool {

    public ResponseEntity<?> handleIncorrectParamException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    public ResponseEntity<?> handleNotFoundException(ItemNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
