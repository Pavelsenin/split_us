package ru.splitus.telegram;

/**
 * Represents telegram message.
 */
public class TelegramMessage {

    private Long messageId;
    private TelegramChat chat;
    private TelegramUser from;
    private String text;

    /**
     * Returns the message id.
     */
    public Long getMessageId() {
        return messageId;
    }

    /**
     * Updates the message id.
     */
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    /**
     * Returns the chat.
     */
    public TelegramChat getChat() {
        return chat;
    }

    /**
     * Updates the chat.
     */
    public void setChat(TelegramChat chat) {
        this.chat = chat;
    }

    /**
     * Returns the from.
     */
    public TelegramUser getFrom() {
        return from;
    }

    /**
     * Updates the from.
     */
    public void setFrom(TelegramUser from) {
        this.from = from;
    }

    /**
     * Returns the text.
     */
    public String getText() {
        return text;
    }

    /**
     * Updates the text.
     */
    public void setText(String text) {
        this.text = text;
    }
}




