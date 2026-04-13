package ru.misis.datatransform.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.misis.datatransform.dto.TransformRequestDto;
import ru.misis.datatransform.dto.TransformResponseDto;
import ru.misis.datatransform.facade.ConversionFacade;

@RestController
@RequestMapping("/transform")
public class TransformController {

    private final ConversionFacade conversionFacade;

    public TransformController(ConversionFacade conversionFacade) {
        this.conversionFacade = conversionFacade;
    }

    @PostMapping
    public TransformResponseDto transform(@Valid @RequestBody TransformRequestDto request) {
        return conversionFacade.convert(request);
    }
}
