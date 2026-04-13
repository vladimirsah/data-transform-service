package ru.misis.datatransform.core.router;

import org.springframework.stereotype.Component;
import ru.misis.datatransform.exception.ValidationException;

@Component
public class MessageRouter {

    public MessageFormat defineFormat(String payload) {
        if (payload == null || payload.isBlank()) {
            throw ValidationException.single("EMPTY_PAYLOAD", "payload", "Payload must not be empty");
        }

        String trimmed = payload.trim();
        if (trimmed.startsWith("<")) {
            return MessageFormat.XML;
        }
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return MessageFormat.JSON;
        }

        throw ValidationException.single("UNSUPPORTED_FORMAT", "payload",
                "Payload should be XML or JSON");
    }
}
