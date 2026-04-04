package ru.splitus.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateCheckRequest {

    @NotBlank
    private String title;

    @NotNull
    private Long ownerTelegramUserId;

    @NotBlank
    private String ownerTelegramUsername;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getOwnerTelegramUserId() {
        return ownerTelegramUserId;
    }

    public void setOwnerTelegramUserId(Long ownerTelegramUserId) {
        this.ownerTelegramUserId = ownerTelegramUserId;
    }

    public String getOwnerTelegramUsername() {
        return ownerTelegramUsername;
    }

    public void setOwnerTelegramUsername(String ownerTelegramUsername) {
        this.ownerTelegramUsername = ownerTelegramUsername;
    }
}

