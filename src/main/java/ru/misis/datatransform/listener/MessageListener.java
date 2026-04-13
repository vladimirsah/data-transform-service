package ru.misis.datatransform.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.misis.datatransform.dto.TransformRequestDto;
import ru.misis.datatransform.facade.ConversionFacade;

@Component
public class MessageListener {

    private final ConversionFacade conversionFacade;

    public MessageListener(ConversionFacade conversionFacade) {
        this.conversionFacade = conversionFacade;
    }

    @RabbitListener(queues = "${app.rabbit.queue:transform.queue}")
    public void receive(TransformRequestDto request) {
        conversionFacade.convert(request);
    }
}
