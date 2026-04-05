package ru.splitus.web;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import ru.splitus.config.TelegramWebhookProperties;
import ru.splitus.telegram.TelegramCommandService;
import ru.splitus.telegram.TelegramMessageSender;
import ru.splitus.telegram.TelegramOutgoingMessage;
import ru.splitus.telegram.TelegramUpdate;
import ru.splitus.telegram.TelegramWebhookResult;

/**
 * Tests telegram webhook controller.
 */
class TelegramWebhookControllerTest {

    @Test
    void dispatchesOutgoingMessagesAfterAcceptedWebhook() {
        TelegramWebhookProperties properties = new TelegramWebhookProperties();
        properties.setPathAlias("prod-bot");
        properties.setSecretToken("secret");

        TelegramCommandService commandService = Mockito.mock(TelegramCommandService.class);
        TelegramMessageSender messageSender = Mockito.mock(TelegramMessageSender.class);
        TelegramWebhookResult webhookResult = new TelegramWebhookResult(
                true,
                Collections.singletonList(new TelegramOutgoingMessage(101L, "reply"))
        );
        Mockito.when(commandService.handleUpdate(Mockito.any(TelegramUpdate.class))).thenReturn(webhookResult);

        TelegramWebhookController controller = new TelegramWebhookController(properties, commandService, messageSender);

        ResponseEntity<Map<String, Object>> response = controller.acceptUpdate("prod-bot", "secret", new TelegramUpdate());

        Assertions.assertEquals(202, response.getStatusCodeValue());
        Assertions.assertEquals(Boolean.TRUE, response.getBody().get("accepted"));
        Assertions.assertEquals(Integer.valueOf(1), response.getBody().get("dispatchedMessages"));
        Mockito.verify(messageSender).sendMessages(webhookResult.getOutgoingMessages());
    }

    @Test
    void rejectsWebhookWithWrongSecretWithoutDispatch() {
        TelegramWebhookProperties properties = new TelegramWebhookProperties();
        properties.setPathAlias("prod-bot");
        properties.setSecretToken("secret");

        TelegramCommandService commandService = Mockito.mock(TelegramCommandService.class);
        TelegramMessageSender messageSender = Mockito.mock(TelegramMessageSender.class);
        TelegramWebhookController controller = new TelegramWebhookController(properties, commandService, messageSender);

        ResponseEntity<Map<String, Object>> response = controller.acceptUpdate("prod-bot", "wrong-secret", new TelegramUpdate());

        Assertions.assertEquals(401, response.getStatusCodeValue());
        Mockito.verifyNoInteractions(commandService);
        Mockito.verifyNoInteractions(messageSender);
    }
}
