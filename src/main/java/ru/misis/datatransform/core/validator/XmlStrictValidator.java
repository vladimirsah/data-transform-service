package ru.misis.datatransform.core.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import ru.misis.datatransform.dto.ErrorDto;
import ru.misis.datatransform.exception.ValidationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class XmlStrictValidator {

    private final XmlMapper xmlMapper = new XmlMapper();

    public void validate(String xmlPayload) {
        JsonNode node;
        try {
            node = xmlMapper.readTree(xmlPayload.getBytes());
        } catch (Exception ex) {
            throw ValidationException.single("XML_STRICT_VALIDATION_ERROR", "payload", ex.getMessage());
        }

        JsonNode orderNode = node.path("id").isMissingNode() ? node.path("order") : node;
        List<ErrorDto> errors = new ArrayList<>();

        validateRequiredText(orderNode, "id", errors);
        validateRequiredText(orderNode, "status", errors);
        validateAmount(orderNode, errors);

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
}
