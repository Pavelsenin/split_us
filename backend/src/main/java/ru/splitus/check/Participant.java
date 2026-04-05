package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents participant.
 */
public class Participant {

    private final UUID id;
    private final UUID checkId;
    private final ParticipantType type;
    private final String displayName;
    private final UUID linkedUserId;
    private final UUID mergedIntoParticipantId;
    private final OffsetDateTime createdAt;

    /**
     * Creates a new participant instance.
     */
    public Participant(
            UUID id,
            UUID checkId,
            ParticipantType type,
            String displayName,
            UUID linkedUserId,
            UUID mergedIntoParticipantId,
            OffsetDateTime createdAt) {
        this.id = id;
        this.checkId = checkId;
        this.type = type;
        this.displayName = displayName;
        this.linkedUserId = linkedUserId;
        this.mergedIntoParticipantId = mergedIntoParticipantId;
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
     * Returns the type.
     */
    public ParticipantType getType() {
        return type;
    }

    /**
     * Returns the display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the linked user id.
     */
    public UUID getLinkedUserId() {
        return linkedUserId;
    }

    /**
     * Returns the merged into participant id.
     */
    public UUID getMergedIntoParticipantId() {
        return mergedIntoParticipantId;
    }

    /**
     * Returns the created at.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns whether active.
     */
    public boolean isActive() {
        return mergedIntoParticipantId == null;
    }
}




