package ru.misis.datatransform.exception;

import ru.misis.datatransform.dto.ErrorDto;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<ErrorDto> errors;

    public ValidationException(List<ErrorDto> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public List<ErrorDto> getErrors() {
        return errors;
    }

    public static ValidationException single(String code, String field, String message) {
        return new ValidationException(List.of(new ErrorDto(code, field, message)));
    }

    public static ValidationException multiple(List<ErrorDto> errors) {
        return new ValidationException(new ArrayList<>(errors));
    }
}
