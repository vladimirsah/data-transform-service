package ru.misis.datatransform.core.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import ru.misis.datatransform.exception.ValidationException;

@Component
public class JsonToXmlTransformer {

    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;

    public JsonToXmlTransformer() {
        this.xmlMapper = new XmlMapper();
        this.objectMapper = new ObjectMapper();
    }

    public String transform(String jsonPayload) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonPayload);
            return xmlMapper.writerWithDefaultPrettyPrinter().withRootName("root").writeValueAsString(jsonNode);
        } catch (Exception ex) {
            throw ValidationException.single("JSON_TO_XML_ERROR", "payload", ex.getMessage());
        }
    }
}
