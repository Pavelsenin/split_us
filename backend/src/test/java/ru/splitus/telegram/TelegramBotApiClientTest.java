package ru.splitus.telegram;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestOperations;
import ru.splitus.config.TelegramWebhookProperties;

/**
 * Tests telegram bot api client.
 */
class TelegramBotApiClientTest {

    @Test
    void sendsOutgoingMessageThroughTelegramBotApi() {
        TelegramWebhookProperties properties = new TelegramWebhookProperties();
        properties.setBotToken("secret-token");
        RestOperations restOperations = Mockito.mock(RestOperations.class);
        TelegramBotApiClient client = new TelegramBotApiClient(properties, restOperations);

        client.sendMessages(java.util.Collections.singletonList(new TelegramOutgoingMessage(101L, "hello", 77L)));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        Mockito.verify(restOperations).postForEntity(urlCaptor.capture(), entityCaptor.capture(), Mockito.eq(String.class));

        Assertions.assertEquals("https://api.telegram.org/botsecret-token/sendMessage", urlCaptor.getValue());
        Assertions.assertTrue(entityCaptor.getValue().getBody() instanceof java.util.Map);

        java.util.Map<?, ?> payload = (java.util.Map<?, ?>) entityCaptor.getValue().getBody();
        Assertions.assertEquals(101L, payload.get("chat_id"));
        Assertions.assertEquals("hello", payload.get("text"));
        Assertions.assertEquals(77L, payload.get("reply_to_message_id"));
        Assertions.assertEquals(Boolean.TRUE, payload.get("allow_sending_without_reply"));
    }

    @Test
    void skipsDispatchWhenBotTokenIsMissing() {
        TelegramWebhookProperties properties = new TelegramWebhookProperties();
        RestOperations restOperations = Mockito.mock(RestOperations.class);
        TelegramBotApiClient client = new TelegramBotApiClient(properties, restOperations);

        client.sendMessages(java.util.Collections.singletonList(new TelegramOutgoingMessage(101L, "hello")));

        Mockito.verifyNoInteractions(restOperations);
    }
}
