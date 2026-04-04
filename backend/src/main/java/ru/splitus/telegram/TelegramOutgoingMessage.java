package ru.splitus.telegram;

public class TelegramOutgoingMessage {

    private final Long chatId;
    private final String text;

    public TelegramOutgoingMessage(Long chatId, String text) {
        this.chatId = chatId;
        this.text = text;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getText() {
        return text;
    }
}

