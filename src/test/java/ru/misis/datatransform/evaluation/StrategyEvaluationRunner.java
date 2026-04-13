package ru.misis.datatransform.evaluation;

import ru.misis.datatransform.core.validator.ValidationStrategyType;
import ru.misis.datatransform.dto.ErrorDto;
import ru.misis.datatransform.dto.TransformRequestDto;
import ru.misis.datatransform.exception.ValidationException;
import ru.misis.datatransform.facade.ConversionFacade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class StrategyEvaluationRunner {

    private final ConversionFacade conversionFacade;

    StrategyEvaluationRunner(ConversionFacade conversionFacade) {
        this.conversionFacade = conversionFacade;
    }

    EvaluationResult evaluate(List<EvaluationCase> cases, ValidationStrategyType strategy) {
        int tp = 0;
        int fp = 0;
        int fn = 0;
        int tn = 0;
        double totalUs = 0;
        double totalDiagnosticRecall = 0;

        for (EvaluationCase oneCase : cases) {
            TransformRequestDto request = new TransformRequestDto();
            request.setPayload(oneCase.payload());
            request.setXsdPath(oneCase.xsdPath());
            request.setValidationStrategy(strategy.name());

            runWarmup(request);

            boolean predictedValid;
            Set<String> actualErrorCodes = Set.of();
            long started = System.nanoTime();
            try {
                conversionFacade.convert(request);
                predictedValid = true;
            } catch (ValidationException ex) {
                predictedValid = false;
                actualErrorCodes = extractCodes(ex.getErrors());
            }
            long elapsedUs = (System.nanoTime() - started) / 1_000;
            totalUs += elapsedUs;

            if (oneCase.expectedValid()) {
                if (predictedValid) {
                    tp++;
                } else {
                    fn++;
                }
            } else {
                if (predictedValid) {
                    fp++;
                } else {
                    tn++;
                }

                Set<String> expectedCodes = oneCase.expectedErrorCodes();
                if (expectedCodes != null && !expectedCodes.isEmpty()) {
                    Set<String> intersection = new HashSet<>(expectedCodes);
                    intersection.retainAll(actualErrorCodes);
                    totalDiagnosticRecall += (double) intersection.size() / expectedCodes.size();
                }
            }
        }

        int n = cases.size();
        double e1 = (double) (fp + fn) / n;
        double e2 = totalUs / n;
        long invalidCases = cases.stream().filter(c -> !c.expectedValid()).count();
        double e3 = invalidCases == 0 ? 1.0 : totalDiagnosticRecall / invalidCases;

        return new EvaluationResult(strategy, tp, fp, fn, tn, e1, e2, e3);
    }

    private Set<String> extractCodes(List<ErrorDto> errors) {
        Set<String> codes = new HashSet<>();
        for (ErrorDto error : errors) {
            codes.add(error.code());
        }
        return codes;
    }

    private void runWarmup(TransformRequestDto request) {
        for (int i = 0; i < 3; i++) {
            try {
                conversionFacade.convert(request);
            } catch (ValidationException ignored) {
                // Warmup should not affect measured results.
            }
        }
    }
}
