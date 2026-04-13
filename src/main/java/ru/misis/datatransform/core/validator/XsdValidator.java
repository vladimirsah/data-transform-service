package ru.misis.datatransform.core.validator;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import ru.misis.datatransform.exception.ValidationException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;

@Component
public class XsdValidator {

    public void validate(String xmlPayload, String xsdPath) {
        if (xsdPath == null || xsdPath.isBlank()) {
            throw ValidationException.single("XSD_REQUIRED", "xsdPath",
                    "xsdPath is required for XML validation");
        }

        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(new ClassPathResource(xsdPath).getInputStream()));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlPayload)));
        } catch (SAXException ex) {
            throw ValidationException.single("XML_VALIDATION_ERROR", "payload", ex.getMessage());
        } catch (IOException ex) {
            throw ValidationException.single("XSD_IO_ERROR", "xsdPath", ex.getMessage());
        }
    }
}
