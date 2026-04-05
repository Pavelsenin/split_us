package ru.splitus.telegram;

/**
 * Represents telegram outgoing message.
 */
public class TelegramOutgoingMessage {

    private final Long chatId;
    private final String text;
    private final Long replyToMessageId;

    /**
     * Creates a new telegram outgoing message instance.
     */
    public TelegramOutgoingMessage(Long chatId, String text) {
        this(chatId, text, null);
    }

    /**
     * Creates a new telegram outgoing message instance.
     */
    public TelegramOutgoingMessage(Long chatId, String text, Long replyToMessageId) {
        this.chatId = chatId;
        this.text = text;
        this.replyToMessageId = replyToMessageId;
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

    /**
     * Returns the replied telegram message id.
     */
    public Long getReplyToMessageId() {
        return replyToMessageId;
    }
}



