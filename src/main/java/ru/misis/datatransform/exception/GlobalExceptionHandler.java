package ru.misis.datatransform.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.misis.datatransform.dto.ErrorDto;
import ru.misis.datatransform.dto.TransformResponseDto;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<TransformResponseDto> handleValidation(ValidationException exception) {
        return ResponseEntity.badRequest().body(TransformResponseDto.fail(exception.getErrors(), UUID.randomUUID().toString()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<TransformResponseDto> handleBadRequest(Exception exception) {
        ErrorDto error = new ErrorDto("BAD_REQUEST", "request", exception.getMessage());
        return ResponseEntity.badRequest().body(TransformResponseDto.fail(List.of(error), UUID.randomUUID().toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TransformResponseDto> handleGeneric(Exception exception) {
        ErrorDto error = new ErrorDto("INTERNAL_ERROR", "system", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TransformResponseDto.fail(List.of(error), UUID.randomUUID().toString()));
    }
}
