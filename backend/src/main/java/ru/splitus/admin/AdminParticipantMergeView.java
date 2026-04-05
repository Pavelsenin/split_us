package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Read-only admin projection of a participant merge event.
 */
public class AdminParticipantMergeView {

    private final UUID mergeId;
    private final UUID sourceParticipantId;
    private final String sourceDisplayName;
    private final UUID targetParticipantId;
    private final String targetDisplayName;
    private final UUID performedByParticipantId;
    private final String performedByDisplayName;
    private final OffsetDateTime createdAt;

    /**
     * Creates a new admin participant merge view instance.
     */
    public AdminParticipantMergeView(
            UUID mergeId,
            UUID sourceParticipantId,
            String sourceDisplayName,
            UUID targetParticipantId,
            String targetDisplayName,
            UUID performedByParticipantId,
            String performedByDisplayName,
            OffsetDateTime createdAt) {
        this.mergeId = mergeId;
        this.sourceParticipantId = sourceParticipantId;
        this.sourceDisplayName = sourceDisplayName;
        this.targetParticipantId = targetParticipantId;
        this.targetDisplayName = targetDisplayName;
        this.performedByParticipantId = performedByParticipantId;
        this.performedByDisplayName = performedByDisplayName;
        this.createdAt = createdAt;
    }

    /**
     * Returns the merge id.
     */
    public UUID getMergeId() {
        return mergeId;
    }

    /**
     * Returns the source participant id.
     */
    public UUID getSourceParticipantId() {
        return sourceParticipantId;
    }

    /**
     * Returns the source participant name.
     */
    public String getSourceDisplayName() {
        return sourceDisplayName;
    }

    /**
     * Returns the target participant id.
     */
    public UUID getTargetParticipantId() {
        return targetParticipantId;
    }

    /**
     * Returns the target participant name.
     */
    public String getTargetDisplayName() {
        return targetDisplayName;
    }

    /**
     * Returns the performer participant id.
     */
    public UUID getPerformedByParticipantId() {
        return performedByParticipantId;
    }

    /**
     * Returns the performer participant name.
     */
    public String getPerformedByDisplayName() {
        return performedByDisplayName;
    }

    /**
     * Returns the creation timestamp.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
