package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.UUID;
import ru.splitus.check.ParticipantType;

/**
 * Read-only admin projection of a participant.
 */
public class AdminParticipantView {

    private final UUID participantId;
    private final ParticipantType participantType;
    private final String displayName;
    private final UUID linkedUserId;
    private final String linkedTelegramUsername;
    private final UUID mergedIntoParticipantId;
    private final String mergedIntoDisplayName;
    private final OffsetDateTime createdAt;

    /**
     * Creates a new admin participant view instance.
     */
    public AdminParticipantView(
            UUID participantId,
            ParticipantType participantType,
            String displayName,
            UUID linkedUserId,
            String linkedTelegramUsername,
            UUID mergedIntoParticipantId,
            String mergedIntoDisplayName,
            OffsetDateTime createdAt) {
        this.participantId = participantId;
        this.participantType = participantType;
        this.displayName = displayName;
        this.linkedUserId = linkedUserId;
        this.linkedTelegramUsername = linkedTelegramUsername;
        this.mergedIntoParticipantId = mergedIntoParticipantId;
        this.mergedIntoDisplayName = mergedIntoDisplayName;
        this.createdAt = createdAt;
    }

    /**
     * Returns the participant id.
     */
    public UUID getParticipantId() {
        return participantId;
    }

    /**
     * Returns the participant type.
     */
    public ParticipantType getParticipantType() {
        return participantType;
    }

    /**
     * Returns the display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the linked user id when present.
     */
    public UUID getLinkedUserId() {
        return linkedUserId;
    }

    /**
     * Returns the linked Telegram username when present.
     */
    public String getLinkedTelegramUsername() {
        return linkedTelegramUsername;
    }

    /**
     * Returns the merge target participant id when present.
     */
    public UUID getMergedIntoParticipantId() {
        return mergedIntoParticipantId;
    }

    /**
     * Returns the merge target participant name when present.
     */
    public String getMergedIntoDisplayName() {
        return mergedIntoDisplayName;
    }

    /**
     * Returns the creation timestamp.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns whether the participant is still active.
     */
    public boolean isActive() {
        return mergedIntoParticipantId == null;
    }
}
