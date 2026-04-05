package ru.splitus.web.dto;

import java.util.UUID;
import javax.validation.constraints.NotNull;

/**
 * Represents the merge participant request payload.
 */
public class MergeParticipantRequest {

    @NotNull
    private UUID targetParticipantId;

    @NotNull
    private UUID performedByParticipantId;

    /**
     * Returns the target participant id.
     */
    public UUID getTargetParticipantId() {
        return targetParticipantId;
    }

    /**
     * Updates the target participant id.
     */
    public void setTargetParticipantId(UUID targetParticipantId) {
        this.targetParticipantId = targetParticipantId;
    }

    /**
     * Returns the performed by participant id.
     */
    public UUID getPerformedByParticipantId() {
        return performedByParticipantId;
    }

    /**
     * Updates the performed by participant id.
     */
    public void setPerformedByParticipantId(UUID performedByParticipantId) {
        this.performedByParticipantId = performedByParticipantId;
    }
}




