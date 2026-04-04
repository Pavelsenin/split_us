package ru.splitus.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AddRegisteredParticipantRequest {

    @NotNull
    private Long telegramUserId;

    @NotBlank
    private String telegramUsername;

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public void setTelegramUserId(Long telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    public String getTelegramUsername() {
        return telegramUsername;
    }

    public void setTelegramUsername(String telegramUsername) {
        this.telegramUsername = telegramUsername;
    }
}

