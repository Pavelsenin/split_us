package ru.splitus.web.dto;

import javax.validation.constraints.NotBlank;

public class AddGuestParticipantRequest {

    @NotBlank
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

