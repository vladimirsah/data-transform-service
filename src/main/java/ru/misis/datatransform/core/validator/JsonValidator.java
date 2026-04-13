package ru.misis.datatransform.core.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.misis.datatransform.dto.ErrorDto;
import ru.misis.datatransform.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JsonValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void validateBasic(String jsonPayload) {
        try {
            objectMapper.readTree(jsonPayload);
        } catch (Exception ex) {
            throw ValidationException.single("JSON_VALIDATION_ERROR", "payload", ex.getMessage());
        }
    }

    public void validateStrict(String jsonPayload) {
        JsonNode node;
        try {
            node = objectMapper.readTree(jsonPayload);
        } catch (Exception ex) {
            throw ValidationException.single("JSON_VALIDATION_ERROR", "payload", ex.getMessage());
        }

        List<ErrorDto> errors = new ArrayList<>();
        validateRequiredText(node, "id", errors);
        validateRequiredText(node, "status", errors);
        validateAmount(node, errors);
        validateDate(node, "createdDate", errors);

        if (!errors.isEmpty()) {
            throw ValidationException.multiple(errors);
        }
    }

    private void validateRequiredText(JsonNode node, String field, List<ErrorDto> errors) {
        if (!node.has(field) || node.get(field).asText().isBlank()) {
            errors.add(new ErrorDto("REQUIRED_FIELD", field, "Field '" + field + "' is required"));
        }
    }

    private void validateAmount(JsonNode node, List<ErrorDto> errors) {
        if (!node.has("amount")) {
            errors.add(new ErrorDto("REQUIRED_FIELD", "amount", "Field 'amount' is required"));
            return;
        }
        try {
            BigDecimal amount = node.get("amount").decimalValue();
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(new ErrorDto("INVALID_RANGE", "amount", "Field 'amount' should be positive"));
            }
        } catch (Exception ex) {
            errors.add(new ErrorDto("INVALID_TYPE", "amount", "Field 'amount' should be numeric"));
        }
    }

    private void validateDate(JsonNode node, String field, List<ErrorDto> errors) {
        if (!node.has(field)) {
            return;
        }
        try {
            LocalDate.parse(node.get(field).asText());
        } catch (DateTimeParseException ex) {
            errors.add(new ErrorDto("INVALID_FORMAT", field, "Field '" + field + "' should be in ISO date format"));
        }
    }
}
