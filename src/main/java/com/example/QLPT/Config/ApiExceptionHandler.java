package com.example.QLPT.Config;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> validation(MethodArgumentNotValidException error) {
        return ResponseEntity.badRequest().body(error.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .distinct().collect(Collectors.joining("; ")));
    }
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> database(DataAccessException error) {
        return ResponseEntity.status(409).body(ApiErrors.message(error));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> invalidArgument(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(error.getMessage());
    }
}
