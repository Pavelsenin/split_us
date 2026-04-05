package ru.splitus.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Represents the create check request payload.
 */
public class CreateCheckRequest {

    @NotBlank
    private String title;

    @NotNull
    private Long ownerTelegramUserId;

    @NotBlank
    private String ownerTelegramUsername;

    /**
     * Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates the title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the owner telegram user id.
     */
    public Long getOwnerTelegramUserId() {
        return ownerTelegramUserId;
    }

    /**
     * Updates the owner telegram user id.
     */
    public void setOwnerTelegramUserId(Long ownerTelegramUserId) {
        this.ownerTelegramUserId = ownerTelegramUserId;
    }

    /**
     * Returns the owner telegram username.
     */
    public String getOwnerTelegramUsername() {
        return ownerTelegramUsername;
    }

    /**
     * Updates the owner telegram username.
     */
    public void setOwnerTelegramUsername(String ownerTelegramUsername) {
        this.ownerTelegramUsername = ownerTelegramUsername;
    }
}




