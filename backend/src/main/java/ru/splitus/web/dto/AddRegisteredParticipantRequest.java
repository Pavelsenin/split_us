package ru.splitus.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Represents the add registered participant request payload.
 */
public class AddRegisteredParticipantRequest {

    @NotNull
    private Long telegramUserId;

    @NotBlank
    private String telegramUsername;

    /**
     * Returns the telegram user id.
     */
    public Long getTelegramUserId() {
        return telegramUserId;
    }

    /**
     * Updates the telegram user id.
     */
    public void setTelegramUserId(Long telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    /**
     * Returns the telegram username.
     */
    public String getTelegramUsername() {
        return telegramUsername;
    }

    /**
     * Updates the telegram username.
     */
    public void setTelegramUsername(String telegramUsername) {
        this.telegramUsername = telegramUsername;
    }
}




