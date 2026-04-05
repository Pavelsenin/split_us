package ru.splitus.web.dto;

import javax.validation.constraints.NotBlank;

/**
 * Represents the add guest participant request payload.
 */
public class AddGuestParticipantRequest {

    @NotBlank
    private String displayName;

    /**
     * Returns the display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Updates the display name.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}




