package ru.splitus.web.dto;

import java.util.UUID;
import javax.validation.constraints.NotNull;

public class MergeParticipantRequest {

    @NotNull
    private UUID targetParticipantId;

    @NotNull
    private UUID performedByParticipantId;

    public UUID getTargetParticipantId() {
        return targetParticipantId;
    }

    public void setTargetParticipantId(UUID targetParticipantId) {
        this.targetParticipantId = targetParticipantId;
    }

    public UUID getPerformedByParticipantId() {
        return performedByParticipantId;
    }

    public void setPerformedByParticipantId(UUID performedByParticipantId) {
        this.performedByParticipantId = performedByParticipantId;
    }
}

