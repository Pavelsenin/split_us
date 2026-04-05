package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a participant merge record.
 */
public class ParticipantMergeRecord {

    private final UUID id;
    private final UUID checkId;
    private final UUID sourceParticipantId;
    private final UUID targetParticipantId;
    private final UUID performedByParticipantId;
    private final OffsetDateTime createdAt;

    /**
     * Creates a new participant merge record instance.
     */
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

    /**
     * Returns the id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the check id.
     */
    public UUID getCheckId() {
        return checkId;
    }

    /**
     * Returns the source participant id.
     */
    public UUID getSourceParticipantId() {
        return sourceParticipantId;
    }

    /**
     * Returns the target participant id.
     */
    public UUID getTargetParticipantId() {
        return targetParticipantId;
    }

    /**
     * Returns the performed by participant id.
     */
    public UUID getPerformedByParticipantId() {
        return performedByParticipantId;
    }

    /**
     * Returns the created at.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}




