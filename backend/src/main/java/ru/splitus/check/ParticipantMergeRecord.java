package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ParticipantMergeRecord {

    private final UUID id;
    private final UUID checkId;
    private final UUID sourceParticipantId;
    private final UUID targetParticipantId;
    private final UUID performedByParticipantId;
    private final OffsetDateTime createdAt;

    public ParticipantMergeRecord(
            UUID id,
            UUID checkId,
            UUID sourceParticipantId,
            UUID targetParticipantId,
            UUID performedByParticipantId,
            OffsetDateTime createdAt) {
        this.id = id;
        this.checkId = checkId;
        this.sourceParticipantId = sourceParticipantId;
        this.targetParticipantId = targetParticipantId;
        this.performedByParticipantId = performedByParticipantId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCheckId() {
        return checkId;
    }

    public UUID getSourceParticipantId() {
        return sourceParticipantId;
    }

    public UUID getTargetParticipantId() {
        return targetParticipantId;
    }

    public UUID getPerformedByParticipantId() {
        return performedByParticipantId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

