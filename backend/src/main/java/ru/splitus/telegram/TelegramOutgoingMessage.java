package ru.splitus.telegram;

/**
 * Represents telegram outgoing message.
 */
public class TelegramOutgoingMessage {

    private final Long chatId;
    private final String text;

    /**
     * Creates a new telegram outgoing message instance.
     */
    public TelegramOutgoingMessage(Long chatId, String text) {
        this.chatId = chatId;
        this.text = text;
    }

    /**
     * Returns the chat id.
     */
    public Long getChatId() {
        return chatId;
    }

    /**
     * Returns the text.
     */
    public String getText() {
        return text;
    }
}




