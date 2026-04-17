package ru.misis.datatransform.core.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.misis.datatransform.exception.ValidationException;

class XmlStrictValidatorTest {

    private final XmlStrictValidator validator = new XmlStrictValidator();

    @Test
    void shouldAcceptValidOrder() {
        validator.validate("""
                <order>
                    <id>1</id>
                    <status>NEW</status>
                    <amount>10.5</amount>
                </order>
                """);
    }

    @Test
    void shouldAcceptValidInn10() {
        validator.validate("""
                <order>
                    <id>1</id>
                    <status>NEW</status>
                    <amount>10.5</amount>
                    <inn>5024019810</inn>
                </order>
                """);
    }

    @Test
    void shouldAcceptValidInn12() {
        validator.validate("""
                <order>
                    <id>1</id>
                    <status>NEW</status>
                    <amount>10.5</amount>
                    <inn>770708389301</inn>
                </order>
                """);
    }

    @Test
    void shouldRejectInvalidInnWhenPresent() {
        Assertions.assertThrows(ValidationException.class,
                () -> validator.validate("""
                        <order>
                            <id>1</id>
                            <status>NEW</status>
                            <amount>10.5</amount>
                            <inn>12345</inn>
                        </order>
                        """));
    }

    @Test
    void shouldRejectMissingId() {
        Assertions.assertThrows(ValidationException.class,
                () -> validator.validate("""
                        <order>
                            <status>NEW</status>
                            <amount>10.5</amount>
                        </order>
                        """));
    }

    @Test
    void shouldRejectNonPositiveAmount() {
        Assertions.assertThrows(ValidationException.class,
                () -> validator.validate("""
                        <order>
                            <id>1</id>
                            <status>NEW</status>
                            <amount>0</amount>
                        </order>
                        """));
    }
}
