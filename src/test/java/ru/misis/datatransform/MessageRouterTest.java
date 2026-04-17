package ru.misis.datatransform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.misis.datatransform.core.router.MessageFormat;
import ru.misis.datatransform.core.router.MessageRouter;
import ru.misis.datatransform.exception.ValidationException;

class MessageRouterTest {

    private final MessageRouter messageRouter = new MessageRouter();

    @Test
    void shouldDetectXmlFormat() {
        MessageFormat format = messageRouter.defineFormat("<order><id>1</id></order>");
        Assertions.assertEquals(MessageFormat.XML, format);
    }

    @Test
    void shouldDetectJsonFormat() {
        MessageFormat format = messageRouter.defineFormat("{\"id\": \"1\"}");
        Assertions.assertEquals(MessageFormat.JSON, format);
    }

    @Test
    void shouldDetectJsonArrayFormat() {
        MessageFormat format = messageRouter.defineFormat("[{\"id\": \"1\"}]");
        Assertions.assertEquals(MessageFormat.JSON, format);
    }

    @Test
    void shouldThrowOnUnsupportedPayload() {
        Assertions.assertThrows(ValidationException.class, () -> messageRouter.defineFormat("plain-text"));
    }
}
