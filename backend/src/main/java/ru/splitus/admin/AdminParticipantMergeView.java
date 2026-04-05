package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents admin participant merge view.
 */
public class AdminParticipantMergeView {

    private final UUID id;
    private final String sourceDisplayName;
    private final String targetDisplayName;
    private final String performedByDisplayName;
    private final OffsetDateTime createdAt;

    /**
     * Creates a new admin participant merge view instance.
     */
    public AdminParticipantMergeView(
            UUID id,
            String sourceDisplayName,
            String targetDisplayName,
            String performedByDisplayName,
            OffsetDateTime createdAt) {
        this.id = id;
        this.sourceDisplayName = sourceDisplayName;
        this.targetDisplayName = targetDisplayName;
        this.performedByDisplayName = performedByDisplayName;
        this.createdAt = createdAt;
    }

    /**
     * Returns the id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the source display name.
     */
    public String getSourceDisplayName() {
        return sourceDisplayName;
    }

    /**
     * Returns the target display name.
     */
    public String getTargetDisplayName() {
        return targetDisplayName;
    }

    /**
     * Returns the performed by display name.
     */
    public String getPerformedByDisplayName() {
        return performedByDisplayName;
    }

    /**
     * Returns the created at.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
