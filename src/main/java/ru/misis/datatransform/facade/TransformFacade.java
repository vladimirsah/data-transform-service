package ru.misis.datatransform.facade;

import org.springframework.stereotype.Service;
import ru.misis.datatransform.dto.TransformRequestDto;
import ru.misis.datatransform.dto.TransformResponseDto;

@Service
public class TransformFacade {

    private final ConversionFacade conversionFacade;

    public TransformFacade(ConversionFacade conversionFacade) {
        this.conversionFacade = conversionFacade;
    }

    public TransformResponseDto transform(TransformRequestDto request) {
        return conversionFacade.convert(request);
    }
}
