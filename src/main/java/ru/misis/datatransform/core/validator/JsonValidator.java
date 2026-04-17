package ru.misis.datatransform.core.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.misis.datatransform.dto.ErrorDto;
import ru.misis.datatransform.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JsonValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void validateBasic(String jsonPayload) {
        JsonNode node;
        try {
            node = objectMapper.readTree(jsonPayload);
        } catch (Exception ex) {
            throw ValidationException.single("JSON_VALIDATION_ERROR", "payload", ex.getMessage());
        }

        if (!node.has("id") || node.get("id").asText().isBlank()) {
            throw ValidationException.single("REQUIRED_FIELD", "id", "Field 'id' is required");
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
        validateIso8601Date(node, "createdDate", errors);

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

    private void validateIso8601Date(JsonNode node, String field, List<ErrorDto> errors) {
        if (!node.has(field) || node.get(field).asText().isBlank()) {
            errors.add(new ErrorDto("REQUIRED_FIELD", field, "Field '" + field + "' is required"));
            return;
        }
        String text = node.get(field).asText();
        if (!isIso8601DateTime(text)) {
            errors.add(new ErrorDto("INVALID_FORMAT", field, "Field '" + field + "' should be ISO-8601"));
        }
    }

    private boolean isIso8601DateTime(String text) {
        try {
            OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return true;
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return true;
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }
}
