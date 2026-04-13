package ru.misis.datatransform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.misis.datatransform.core.router.MessageRouter;
import ru.misis.datatransform.core.transformer.JsonToXmlTransformer;
import ru.misis.datatransform.core.transformer.XmlToJsonTransformer;
import ru.misis.datatransform.core.validator.JsonValidator;
import ru.misis.datatransform.core.validator.ValidationService;
import ru.misis.datatransform.core.validator.XsdValidator;
import ru.misis.datatransform.core.validator.XmlStrictValidator;
import ru.misis.datatransform.dto.TransformRequestDto;
import ru.misis.datatransform.dto.TransformResponseDto;
import ru.misis.datatransform.exception.ValidationException;
import ru.misis.datatransform.facade.ConversionFacade;

class TransformFacadeTest {

    private final ConversionFacade conversionFacade = new ConversionFacade(
            new MessageRouter(),
            new ValidationService(new XsdValidator(), new XmlStrictValidator(), new JsonValidator()),
            new XmlToJsonTransformer(),
            new JsonToXmlTransformer()
    );

    @Test
    void shouldTransformValidXmlToJson() {
        TransformRequestDto request = new TransformRequestDto();
        request.setXsdPath("xsd/sample-order.xsd");
        request.setPayload("""
                <order>
                    <id>100</id>
                    <status>NEW</status>
                    <amount>77.5</amount>
                </order>
                """);

        TransformResponseDto response = conversionFacade.convert(request);

        Assertions.assertEquals("OK", response.getStatus());
        Assertions.assertTrue(response.getData().contains("\"id\""));
        Assertions.assertNotNull(response.getProcessingTimeMs());
    }

    @Test
    void shouldFailForInvalidXml() {
        TransformRequestDto request = new TransformRequestDto();
        request.setXsdPath("xsd/sample-order.xsd");
        request.setValidationStrategy("V2_XML_XSD_STRICT");
        request.setPayload("<order><id>100</id></order>");

        Assertions.assertThrows(ValidationException.class, () -> conversionFacade.convert(request));
    }

    @Test
    void shouldTransformJsonWithStrictStrategy() {
        TransformRequestDto request = new TransformRequestDto();
        request.setValidationStrategy("V4_JSON_SCHEMA_STRICT");
        request.setPayload("""
                {
                  "id": "100",
                  "status": "NEW",
                  "amount": 77.5,
                  "createdDate": "2026-04-13"
                }
                """);

        TransformResponseDto response = conversionFacade.convert(request);
        Assertions.assertEquals("OK", response.getStatus());
        Assertions.assertEquals("V4_JSON_SCHEMA_STRICT", response.getValidationStrategy());
    }
}
