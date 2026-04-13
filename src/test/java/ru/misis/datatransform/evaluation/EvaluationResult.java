package ru.misis.datatransform.evaluation;

import ru.misis.datatransform.core.validator.ValidationStrategyType;

record EvaluationResult(
        ValidationStrategyType strategy,
        int tp,
        int fp,
        int fn,
        int tn,
        double e1,
        double e2,
        double e3
) {
}
