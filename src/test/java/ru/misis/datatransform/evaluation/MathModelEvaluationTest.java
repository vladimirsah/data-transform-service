package ru.misis.datatransform.evaluation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.misis.datatransform.core.router.MessageRouter;
import ru.misis.datatransform.core.transformer.JsonToXmlTransformer;
import ru.misis.datatransform.core.transformer.XmlToJsonTransformer;
import ru.misis.datatransform.core.validator.JsonValidator;
import ru.misis.datatransform.core.validator.ValidationService;
import ru.misis.datatransform.core.validator.ValidationStrategyType;
import ru.misis.datatransform.core.validator.XsdValidator;
import ru.misis.datatransform.core.validator.XmlStrictValidator;
import ru.misis.datatransform.facade.ConversionFacade;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class MathModelEvaluationTest {

    private static final List<double[]> SENSITIVITY_WEIGHTS = List.of(
            new double[]{0.5, 0.2, 0.3},
            new double[]{0.6, 0.2, 0.2},
            new double[]{0.4, 0.2, 0.4}
    );

    private final ConversionFacade conversionFacade = new ConversionFacade(
            new MessageRouter(),
            new ValidationService(new XsdValidator(), new XmlStrictValidator(), new JsonValidator()),
            new XmlToJsonTransformer(),
            new JsonToXmlTransformer()
    );

    private final StrategyEvaluationRunner runner = new StrategyEvaluationRunner(conversionFacade);

    @Test
    void shouldCalculateBestStrategyForXmlAndJson() {
        List<EvaluationCase> xmlCases = xmlCases();
        List<EvaluationCase> jsonCases = jsonCases();

        List<EvaluationResult> xmlResults = List.of(
                runner.evaluate(xmlCases, ValidationStrategyType.V1_XML_XSD_BASIC),
                runner.evaluate(xmlCases, ValidationStrategyType.V2_XML_XSD_STRICT)
        );
        List<EvaluationResult> jsonResults = List.of(
                runner.evaluate(jsonCases, ValidationStrategyType.V3_JSON_SCHEMA_BASIC),
                runner.evaluate(jsonCases, ValidationStrategyType.V4_JSON_SCHEMA_STRICT)
        );

        runSensitivityAnalysis("XML", xmlResults);
        runSensitivityAnalysis("JSON", jsonResults);

        logQueueThreshold(xmlResults);

        Map<ValidationStrategyType, Double> xmlScores = calculateScores(xmlResults, 0.5, 0.2, 0.3);
        Map<ValidationStrategyType, Double> jsonScores = calculateScores(jsonResults, 0.5, 0.2, 0.3);

        printTable("XML", xmlResults, xmlScores);
        printTable("JSON", jsonResults, jsonScores);
        exportResults("xml", xmlResults, xmlScores);
        exportResults("json", jsonResults, jsonScores);

        ValidationStrategyType bestXml = minScore(xmlScores);
        ValidationStrategyType bestJson = minScore(jsonScores);

        Assertions.assertNotNull(bestXml);
        Assertions.assertNotNull(bestJson);

        double e3V1 = e3For(xmlResults, ValidationStrategyType.V1_XML_XSD_BASIC);
        double e3V2 = e3For(xmlResults, ValidationStrategyType.V2_XML_XSD_STRICT);
        Assertions.assertTrue(
                e3V1 < e3V2,
                () -> String.format(Locale.US, "E3(V1)=%.4f should be < E3(V2)=%.4f (semantic defects invisible to XSD)", e3V1, e3V2)
        );
    }

    private void runSensitivityAnalysis(String label, List<EvaluationResult> results) {
        System.out.println("=== Sensitivity analysis (" + label + ") ===");
        for (double[] w : SENSITIVITY_WEIGHTS) {
            Map<ValidationStrategyType, Double> scores = calculateScores(results, w[0], w[1], w[2]);
            ValidationStrategyType best = minScore(scores);
            System.out.printf(
                    Locale.US,
                    "weights (w1=%.1f, w2=%.1f, w3=%.1f) -> bestStrategy=%s%n",
                    w[0], w[1], w[2], best
            );
        }
    }

    private void logQueueThreshold(List<EvaluationResult> xmlResults) {
        double e2V1 = e2For(xmlResults, ValidationStrategyType.V1_XML_XSD_BASIC);
        double e2V2 = e2For(xmlResults, ValidationStrategyType.V2_XML_XSD_STRICT);
        if (e2V1 <= 0 || e2V2 <= 0) {
            System.out.println("=== M/M/1 threshold λ* (XML): skipped (non-positive E2)");
            return;
        }
        double mu1PerSecond = 1_000_000.0 / e2V1;
        double mu2PerSecond = 1_000_000.0 / e2V2;
        double lambdaStar = mu2PerSecond - mu1PerSecond;
        System.out.println("=== M/M/1 threshold λ* (XML, from measured E2 in µs) ===");
        System.out.printf(Locale.US, "E2(V1)=%.2f µs, E2(V2)=%.2f µs%n", e2V1, e2V2);
        System.out.printf(Locale.US, "μ1=1/E2(V1)=%.6f s⁻¹, μ2=1/E2(V2)=%.6f s⁻¹%n", mu1PerSecond, mu2PerSecond);
        System.out.printf(Locale.US, "λ* = μ2 - μ1 = %.6f s⁻¹%n", lambdaStar);
    }

    private double e2For(List<EvaluationResult> results, ValidationStrategyType type) {
        return results.stream()
                .filter(r -> r.strategy() == type)
                .mapToDouble(EvaluationResult::e2)
                .findFirst()
                .orElse(0.0);
    }

    private double e3For(List<EvaluationResult> results, ValidationStrategyType type) {
        return results.stream()
                .filter(r -> r.strategy() == type)
                .mapToDouble(EvaluationResult::e3)
                .findFirst()
                .orElse(0.0);
    }

    private void exportResults(String format, List<EvaluationResult> results, Map<ValidationStrategyType, Double> scores) {
        Path outputDir = Path.of("results");
        try {
            Files.createDirectories(outputDir);
            writeCsv(outputDir.resolve(format + "_results.csv"), results, scores);
            writeMarkdown(outputDir.resolve(format + "_results.md"), format.toUpperCase(), results, scores);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to export evaluation results", ex);
        }
    }

    private void writeCsv(Path file, List<EvaluationResult> results, Map<ValidationStrategyType, Double> scores) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("strategy,E1,E2_us,E3,F,TP,FP,FN,TN\n");
        for (EvaluationResult result : results) {
            sb.append(result.strategy().name()).append(",")
                    .append(format(result.e1())).append(",")
                    .append(format(result.e2())).append(",")
                    .append(format(result.e3())).append(",")
                    .append(format(scores.get(result.strategy()))).append(",")
                    .append(result.tp()).append(",")
                    .append(result.fp()).append(",")
                    .append(result.fn()).append(",")
                    .append(result.tn()).append("\n");
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    private void writeMarkdown(Path file, String formatName, List<EvaluationResult> results,
                               Map<ValidationStrategyType, Double> scores) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(formatName).append(" strategy evaluation\n\n");
        sb.append("| Strategy | E1 | E2 (us) | E3 | F(V) | TP | FP | FN | TN |\n");
        sb.append("|---|---:|---:|---:|---:|---:|---:|---:|---:|\n");
        for (EvaluationResult result : results) {
            sb.append("| ").append(result.strategy().name()).append(" | ")
                    .append(format(result.e1())).append(" | ")
                    .append(format(result.e2())).append(" | ")
                    .append(format(result.e3())).append(" | ")
                    .append(format(scores.get(result.strategy()))).append(" | ")
                    .append(result.tp()).append(" | ")
                    .append(result.fp()).append(" | ")
                    .append(result.fn()).append(" | ")
                    .append(result.tn()).append(" |\n");
        }
        ValidationStrategyType best = minScore(scores);
        sb.append("\nBest strategy (default weights 0.5/0.2/0.3): `").append(best).append("`\n");

        sb.append("\n## Sensitivity analysis\n\n");
        sb.append("| w1 | w2 | w3 | bestStrategy |\n");
        sb.append("|---:|---:|---:|---|\n");
        for (double[] w : SENSITIVITY_WEIGHTS) {
            Map<ValidationStrategyType, Double> s = calculateScores(results, w[0], w[1], w[2]);
            sb.append("| ").append(format(w[0])).append(" | ")
                    .append(format(w[1])).append(" | ")
                    .append(format(w[2])).append(" | `")
                    .append(minScore(s)).append("` |\n");
        }

        if ("XML".equalsIgnoreCase(formatName)) {
            double e2V1 = e2For(results, ValidationStrategyType.V1_XML_XSD_BASIC);
            double e2V2 = e2For(results, ValidationStrategyType.V2_XML_XSD_STRICT);
            if (e2V1 > 0 && e2V2 > 0) {
                double mu1 = 1_000_000.0 / e2V1;
                double mu2 = 1_000_000.0 / e2V2;
                sb.append("\n## M/M/1 threshold (XML)\n\n");
                sb.append("λ* = μ2 − μ1, μ = 1/E2 (E2 in µs, μ in s⁻¹): **")
                        .append(format(mu2 - mu1)).append("** s⁻¹\n");
            }
        }

        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    private String format(double value) {
        return String.format(Locale.US, "%.4f", value);
    }

    private Map<ValidationStrategyType, Double> calculateScores(List<EvaluationResult> results, double w1, double w2, double w3) {
        double maxE1 = results.stream().mapToDouble(EvaluationResult::e1).max().orElse(1.0);
        double maxE2 = results.stream().mapToDouble(EvaluationResult::e2).max().orElse(1.0);
        double maxE3 = results.stream().mapToDouble(EvaluationResult::e3).max().orElse(1.0);

        return results.stream().collect(Collectors.toMap(
                EvaluationResult::strategy,
                r -> {
                    double e1Norm = maxE1 == 0 ? 0 : r.e1() / maxE1;
                    double e2Norm = maxE2 == 0 ? 0 : r.e2() / maxE2;
                    double e3Norm = maxE3 == 0 ? 1 : 1 - (r.e3() / maxE3);
                    return w1 * e1Norm + w2 * e2Norm + w3 * e3Norm;
                }
        ));
    }

    private ValidationStrategyType minScore(Map<ValidationStrategyType, Double> scores) {
        return scores.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private void printTable(String format, List<EvaluationResult> results, Map<ValidationStrategyType, Double> scores) {
        System.out.println("=== " + format + " Strategy Results ===");
        for (EvaluationResult result : results) {
            double score = scores.get(result.strategy());
            System.out.printf(
                    Locale.US,
                    "%s | E1=%.4f | E2=%.2f us | E3=%.4f | F=%.4f | TP=%d FP=%d FN=%d TN=%d%n",
                    result.strategy().name(),
                    result.e1(),
                    result.e2(),
                    result.e3(),
                    score,
                    result.tp(),
                    result.fp(),
                    result.fn(),
                    result.tn()
            );
        }
    }

    private List<EvaluationCase> xmlCases() {
        List<EvaluationCase> cases = new ArrayList<>();
        String xsd = "xsd/sample-order.xsd";

        for (int i = 1; i <= 20; i++) {
            String inn = i <= 10 ? "<inn>502401981" + (i % 10) + "</inn>" : "";
            cases.add(new EvaluationCase(
                    "xml-valid-" + i,
                    "<order><id>" + i + "</id><status>NEW</status><amount>" + (10 + i) + ".0</amount>" + inn + "</order>",
                    xsd,
                    true,
                    Set.of(),
                    ValidationStrategyType.V1_XML_XSD_BASIC
            ));
        }

        for (int i = 1; i <= 5; i++) {
            int id = 200 + i;
            cases.add(new EvaluationCase(
                    "xml-invalid-missing-amount-" + id,
                    "<order><id>" + id + "</id><status>NEW</status></order>",
                    xsd,
                    false,
                    Set.of("XML_VALIDATION_ERROR"),
                    ValidationStrategyType.V1_XML_XSD_BASIC
            ));
        }

        for (int i = 1; i <= 5; i++) {
            int id = 210 + i;
            cases.add(new EvaluationCase(
                    "xml-invalid-missing-status-" + id,
                    "<order><id>" + id + "</id><amount>" + (10 + id) + ".0</amount></order>",
                    xsd,
                    false,
                    Set.of("XML_VALIDATION_ERROR"),
                    ValidationStrategyType.V1_XML_XSD_BASIC
            ));
        }

        for (int i = 1; i <= 5; i++) {
            int id = 220 + i;
            cases.add(new EvaluationCase(
                    "xml-invalid-inn-" + id,
                    "<order><id>" + id + "</id><status>NEW</status><amount>50.0</amount><inn>12345</inn></order>",
                    xsd,
                    false,
                    Set.of("INN_FORMAT_ERROR"),
                    ValidationStrategyType.V2_XML_XSD_STRICT
            ));
        }

        for (int i = 1; i <= 3; i++) {
            int id = 230 + i;
            cases.add(new EvaluationCase(
                    "xml-invalid-amount-not-decimal-" + id,
                    "<order><id>" + id + "</id><status>NEW</status><amount>not-a-decimal</amount></order>",
                    xsd,
                    false,
                    Set.of("XML_VALIDATION_ERROR"),
                    ValidationStrategyType.V1_XML_XSD_BASIC
            ));
        }

        for (int i = 1; i <= 2; i++) {
            int id = 240 + i;
            cases.add(new EvaluationCase(
                    "xml-invalid-amount-negative-" + id,
                    "<order><id>" + id + "</id><status>NEW</status><amount>-5.0</amount></order>",
                    xsd,
                    false,
                    Set.of("INVALID_RANGE"),
                    ValidationStrategyType.V2_XML_XSD_STRICT
            ));
        }

        return cases;
    }

    private List<EvaluationCase> jsonCases() {
        List<EvaluationCase> cases = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            cases.add(new EvaluationCase(
                    "json-valid-" + i,
                    "{\"id\":\"j" + i + "\",\"status\":\"NEW\",\"amount\":" + (100 + i)
                            + ",\"createdDate\":\"2026-04-13T10:15:30+03:00\"}",
                    null,
                    true,
                    Set.of(),
                    ValidationStrategyType.V3_JSON_SCHEMA_BASIC
            ));
        }

        for (int i = 21; i <= 25; i++) {
            cases.add(new EvaluationCase(
                    "json-invalid-missing-id-" + i,
                    "{\"status\":\"NEW\",\"amount\":77.7,\"createdDate\":\"2026-04-13\"}",
                    null,
                    false,
                    Set.of("REQUIRED_FIELD"),
                    ValidationStrategyType.V4_JSON_SCHEMA_STRICT
            ));
        }

        for (int i = 26; i <= 30; i++) {
            cases.add(new EvaluationCase(
                    "json-invalid-type-" + i,
                    "{\"id\":\"j" + i + "\",\"status\":\"NEW\",\"amount\":\"abc\",\"createdDate\":\"2026-04-13\"}",
                    null,
                    false,
                    Set.of("INVALID_TYPE"),
                    ValidationStrategyType.V4_JSON_SCHEMA_STRICT
            ));
        }

        for (int i = 31; i <= 35; i++) {
            cases.add(new EvaluationCase(
                    "json-invalid-missing-status-" + i,
                    "{\"id\":\"j" + i + "\",\"amount\":77.7,\"createdDate\":\"2026-04-13\"}",
                    null,
                    false,
                    Set.of("REQUIRED_FIELD"),
                    ValidationStrategyType.V4_JSON_SCHEMA_STRICT
            ));
        }

        for (int i = 36; i <= 40; i++) {
            cases.add(new EvaluationCase(
                    "json-invalid-date-" + i,
                    "{\"id\":\"j" + i + "\",\"status\":\"NEW\",\"amount\":55.5,\"createdDate\":\"13.04.2026\"}",
                    null,
                    false,
                    Set.of("INVALID_FORMAT"),
                    ValidationStrategyType.V4_JSON_SCHEMA_STRICT
            ));
        }
        return cases;
    }
}
