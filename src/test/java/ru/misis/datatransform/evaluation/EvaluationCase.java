package ru.misis.datatransform.evaluation;

import ru.misis.datatransform.core.validator.ValidationStrategyType;

import java.util.Set;

record EvaluationCase(
        String name,
        String payload,
        String xsdPath,
        boolean expectedValid,
        Set<String> expectedErrorCodes,
        ValidationStrategyType strategy
) {
}
