package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Participant {

    private final UUID id;
    private final UUID checkId;
    private final ParticipantType type;
    private final String displayName;
    private final UUID linkedUserId;
    private final UUID mergedIntoParticipantId;
    private final OffsetDateTime createdAt;

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

    public UUID getId() {
        return id;
    }

    public UUID getCheckId() {
        return checkId;
    }

    public ParticipantType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UUID getLinkedUserId() {
        return linkedUserId;
    }

    public UUID getMergedIntoParticipantId() {
        return mergedIntoParticipantId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return mergedIntoParticipantId == null;
    }
}

