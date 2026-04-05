package ru.splitus.telegram;

/**
 * Represents telegram update.
 */
public class TelegramUpdate {

    private Long updateId;
    private TelegramMessage message;
    private TelegramMessage editedMessage;

    /**
     * Returns the update id.
     */
    public Long getUpdateId() {
        return updateId;
    }

    /**
     * Updates the update id.
     */
    public void setUpdateId(Long updateId) {
        this.updateId = updateId;
    }

    /**
     * Returns the message.
     */
    public TelegramMessage getMessage() {
        return message;
    }

    /**
     * Updates the message.
     */
    public void setMessage(TelegramMessage message) {
        this.message = message;
    }

    /**
     * Returns the edited message.
     */
    public TelegramMessage getEditedMessage() {
        return editedMessage;
    }

    /**
     * Updates the edited message.
     */
    public void setEditedMessage(TelegramMessage editedMessage) {
        this.editedMessage = editedMessage;
    }
}



