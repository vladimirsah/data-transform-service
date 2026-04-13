package ru.misis.datatransform.facade;

import org.springframework.stereotype.Service;
import ru.misis.datatransform.core.router.MessageFormat;
import ru.misis.datatransform.core.router.MessageRouter;
import ru.misis.datatransform.core.transformer.JsonToXmlTransformer;
import ru.misis.datatransform.core.transformer.XmlToJsonTransformer;
import ru.misis.datatransform.core.validator.ValidationService;
import ru.misis.datatransform.core.validator.ValidationStrategyType;
import ru.misis.datatransform.dto.TransformRequestDto;
import ru.misis.datatransform.dto.TransformResponseDto;

import java.util.UUID;

@Service
public class ConversionFacade {

    private final MessageRouter messageRouter;
    private final ValidationService validationService;
    private final XmlToJsonTransformer xmlToJsonTransformer;
    private final JsonToXmlTransformer jsonToXmlTransformer;

    public ConversionFacade(MessageRouter messageRouter,
                            ValidationService validationService,
                            XmlToJsonTransformer xmlToJsonTransformer,
                            JsonToXmlTransformer jsonToXmlTransformer) {
        this.messageRouter = messageRouter;
        this.validationService = validationService;
        this.xmlToJsonTransformer = xmlToJsonTransformer;
        this.jsonToXmlTransformer = jsonToXmlTransformer;
    }

    public TransformResponseDto convert(TransformRequestDto request) {
        long startedAt = System.nanoTime();
        String traceId = UUID.randomUUID().toString();

        MessageFormat format = messageRouter.defineFormat(request.getPayload());
        ValidationStrategyType strategy = validationService.validate(request, format);

        String result = format == MessageFormat.XML
                ? xmlToJsonTransformer.transform(request.getPayload())
                : jsonToXmlTransformer.transform(request.getPayload());

        long processingTimeMs = (System.nanoTime() - startedAt) / 1_000_000;
        return TransformResponseDto.ok(result, traceId, processingTimeMs, strategy.name());
    }
}
