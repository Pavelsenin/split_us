package ru.splitus.telegram;

import java.util.List;

/**
 * Sends outgoing telegram messages through the Bot API.
 */
public interface TelegramMessageSender {

    /**
     * Sends all outgoing messages from the webhook result.
     */
    void sendMessages(List<TelegramOutgoingMessage> outgoingMessages);
}
