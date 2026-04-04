package ru.splitus.telegram;

public class TelegramUpdate {

    private Long updateId;
    private TelegramMessage message;
    private TelegramMessage editedMessage;

    public Long getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Long updateId) {
        this.updateId = updateId;
    }

    public TelegramMessage getMessage() {
        return message;
    }

    public void setMessage(TelegramMessage message) {
        this.message = message;
    }

    public TelegramMessage getEditedMessage() {
        return editedMessage;
    }

    public void setEditedMessage(TelegramMessage editedMessage) {
        this.editedMessage = editedMessage;
    }
}
