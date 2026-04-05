package ru.splitus.telegram;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import ru.splitus.config.TelegramWebhookProperties;

/**
 * Sends outgoing telegram messages through the HTTP Bot API.
 */
@Component
public class TelegramBotApiClient implements TelegramMessageSender {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotApiClient.class);

    private final TelegramWebhookProperties properties;
    private final RestOperations restOperations;

    /**
     * Creates a new telegram bot api client instance.
     */
    @Autowired
    public TelegramBotApiClient(TelegramWebhookProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this(properties, restTemplateBuilder.build());
    }

    /**
     * Creates a new telegram bot api client instance.
     */
    TelegramBotApiClient(TelegramWebhookProperties properties, RestOperations restOperations) {
        this.properties = properties;
        this.restOperations = restOperations;
    }

    /**
     * Sends all outgoing messages through Telegram Bot API.
     */
    @Override
    public void sendMessages(List<TelegramOutgoingMessage> outgoingMessages) {
        if (outgoingMessages == null || outgoingMessages.isEmpty()) {
            return;
        }

        if (!hasText(properties.getBotToken())) {
            log.error("Telegram reply dispatch skipped because bot token is not configured");
            return;
        }

        for (TelegramOutgoingMessage outgoingMessage : outgoingMessages) {
            sendMessage(outgoingMessage);
        }
    }

    private void sendMessage(TelegramOutgoingMessage outgoingMessage) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("chat_id", outgoingMessage.getChatId());
        payload.put("text", outgoingMessage.getText());
        if (outgoingMessage.getReplyToMessageId() != null) {
            payload.put("reply_to_message_id", outgoingMessage.getReplyToMessageId());
            payload.put("allow_sending_without_reply", Boolean.TRUE);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restOperations.postForEntity(
                "https://api.telegram.org/bot" + properties.getBotToken().trim() + "/sendMessage",
                new HttpEntity<Map<String, Object>>(payload, headers),
                String.class
        );

        log.info("Telegram reply sent: chatId={} replyToMessageId={}",
                outgoingMessage.getChatId(),
                outgoingMessage.getReplyToMessageId());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
