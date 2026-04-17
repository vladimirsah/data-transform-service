package ru.misis.datatransform.core.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.misis.datatransform.exception.ValidationException;

class JsonValidatorTest {

    private final JsonValidator jsonValidator = new JsonValidator();

    @Test
    void validateBasicShouldAcceptPayloadWithId() {
        jsonValidator.validateBasic("{\"id\":\"x\"}");
    }

    @Test
    void validateBasicShouldRejectMissingId() {
        Assertions.assertThrows(ValidationException.class,
                () -> jsonValidator.validateBasic("{\"status\":\"NEW\"}"));
    }

    @Test
    void validateStrictShouldAcceptIso8601DateVariants() {
        jsonValidator.validateStrict("""
                {"id":"1","status":"NEW","amount":10,"createdDate":"2026-04-13"}
                """);
        jsonValidator.validateStrict("""
                {"id":"1","status":"NEW","amount":10,"createdDate":"2026-04-13T10:15:30"}
                """);
        jsonValidator.validateStrict("""
                {"id":"1","status":"NEW","amount":10,"createdDate":"2026-04-13T10:15:30+03:00"}
                """);
    }

    @Test
    void validateStrictShouldRejectInvalidAmountType() {
        Assertions.assertThrows(ValidationException.class,
                () -> jsonValidator.validateStrict("""
                        {"id":"1","status":"NEW","amount":"nope","createdDate":"2026-04-13"}
                        """));
    }

    @Test
    void validateStrictShouldRejectInvalidDate() {
        Assertions.assertThrows(ValidationException.class,
                () -> jsonValidator.validateStrict("""
                        {"id":"1","status":"NEW","amount":10,"createdDate":"13.04.2026"}
                        """));
    }

    @Test
    void validateStrictShouldRejectMissingCreatedDate() {
        Assertions.assertThrows(ValidationException.class,
                () -> jsonValidator.validateStrict("""
                        {"id":"1","status":"NEW","amount":10}
                        """));
    }
}
