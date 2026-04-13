package ru.misis.datatransform.core.validator;

import org.springframework.stereotype.Service;
import ru.misis.datatransform.core.router.MessageFormat;
import ru.misis.datatransform.dto.TransformRequestDto;
import ru.misis.datatransform.exception.ValidationException;

@Service
public class ValidationService {

    private final XsdValidator xsdValidator;
    private final XmlStrictValidator xmlStrictValidator;
    private final JsonValidator jsonValidator;

    public ValidationService(XsdValidator xsdValidator,
                             XmlStrictValidator xmlStrictValidator,
                             JsonValidator jsonValidator) {
        this.xsdValidator = xsdValidator;
        this.xmlStrictValidator = xmlStrictValidator;
        this.jsonValidator = jsonValidator;
    }

    public ValidationStrategyType validate(TransformRequestDto request, MessageFormat detectedFormat) {
        ValidationStrategyType strategy = resolveStrategy(request.getValidationStrategy(), detectedFormat);

        switch (strategy) {
            case V1_XML_XSD_BASIC -> xsdValidator.validate(request.getPayload(), request.getXsdPath());
            case V2_XML_XSD_STRICT -> {
                xsdValidator.validate(request.getPayload(), request.getXsdPath());
                xmlStrictValidator.validate(request.getPayload());
            }
            case V3_JSON_SCHEMA_BASIC -> jsonValidator.validateBasic(request.getPayload());
            case V4_JSON_SCHEMA_STRICT -> jsonValidator.validateStrict(request.getPayload());
            case AUTO -> throw ValidationException.single("STRATEGY_ERROR", "validationStrategy",
                    "AUTO strategy should be resolved before validation");
        }

        return strategy;
    }

    private ValidationStrategyType resolveStrategy(String rawStrategy, MessageFormat detectedFormat) {
        if (rawStrategy == null || rawStrategy.isBlank() || "AUTO".equalsIgnoreCase(rawStrategy)) {
            return detectedFormat == MessageFormat.XML
                    ? ValidationStrategyType.V1_XML_XSD_BASIC
                    : ValidationStrategyType.V3_JSON_SCHEMA_BASIC;
        }

        ValidationStrategyType strategy;
        try {
            strategy = ValidationStrategyType.valueOf(rawStrategy.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ValidationException.single("UNKNOWN_STRATEGY", "validationStrategy",
                    "Unknown strategy: " + rawStrategy);
        }

        if (detectedFormat == MessageFormat.XML &&
                (strategy == ValidationStrategyType.V3_JSON_SCHEMA_BASIC || strategy == ValidationStrategyType.V4_JSON_SCHEMA_STRICT)) {
            throw ValidationException.single("STRATEGY_FORMAT_MISMATCH", "validationStrategy",
                    "JSON strategy cannot be used for XML payload");
        }

        if (detectedFormat == MessageFormat.JSON &&
                (strategy == ValidationStrategyType.V1_XML_XSD_BASIC || strategy == ValidationStrategyType.V2_XML_XSD_STRICT)) {
            throw ValidationException.single("STRATEGY_FORMAT_MISMATCH", "validationStrategy",
                    "XML strategy cannot be used for JSON payload");
        }

        return strategy;
    }
}
