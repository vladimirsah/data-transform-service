package ru.misis.datatransform.core.validator;

import org.springframework.stereotype.Component;
import ru.misis.datatransform.dto.ErrorDto;
import ru.misis.datatransform.exception.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class XmlStrictValidator {

    private static final Pattern INN_PATTERN = Pattern.compile("^\\d{10}(\\d{2})?$");

    public void validate(String xmlPayload) {
        Document document;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlPayload)));
        } catch (Exception ex) {
            throw ValidationException.single("XML_STRICT_VALIDATION_ERROR", "payload", ex.getMessage());
        }

        List<ErrorDto> errors = new ArrayList<>();

        validateRequiredText(document, "id", errors);
        validateRequiredText(document, "status", errors);
        validateAmount(document, errors);
        validateInn(document, errors);

        if (!errors.isEmpty()) {
            throw ValidationException.multiple(errors);
        }
    }

    private void validateRequiredText(Document document, String field, List<ErrorDto> errors) {
        String value = getFirstTagValue(document, field);
        if (value == null || value.isBlank()) {
            errors.add(new ErrorDto("REQUIRED_FIELD", field, "Field '" + field + "' is required"));
        }
    }

    private void validateAmount(Document document, List<ErrorDto> errors) {
        String value = getFirstTagValue(document, "amount");
        if (value == null || value.isBlank()) {
            errors.add(new ErrorDto("REQUIRED_FIELD", "amount", "Field 'amount' is required"));
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(value);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(new ErrorDto("INVALID_RANGE", "amount", "Field 'amount' should be positive"));
            }
        } catch (Exception ex) {
            errors.add(new ErrorDto("INVALID_TYPE", "amount", "Field 'amount' should be numeric"));
        }
    }

    private void validateInn(Document document, List<ErrorDto> errors) {
        String inn = getFirstTagValue(document, "inn");
        if (inn == null || inn.isBlank()) {
            return;
        }
        if (!INN_PATTERN.matcher(inn.trim()).matches()) {
            errors.add(new ErrorDto("INN_FORMAT_ERROR", "inn", "INN must match pattern ^\\d{10}(\\d{2})?$"));
        }
    }

    private String getFirstTagValue(Document document, String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent();
    }
}
