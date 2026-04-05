package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.UUID;
import ru.splitus.check.ParticipantType;

/**
 * Represents admin participant view.
 */
public class AdminParticipantView {

    private final UUID id;
    private final ParticipantType type;
    private final String displayName;
    private final String linkedTelegramUsername;
    private final UUID mergedIntoParticipantId;
    private final boolean active;
    private final OffsetDateTime createdAt;

    /**
     * Creates a new admin participant view instance.
     */
    public AdminParticipantView(
            UUID id,
            ParticipantType type,
            String displayName,
            String linkedTelegramUsername,
            UUID mergedIntoParticipantId,
            boolean active,
            OffsetDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.displayName = displayName;
        this.linkedTelegramUsername = linkedTelegramUsername;
        this.mergedIntoParticipantId = mergedIntoParticipantId;
        this.active = active;
        this.createdAt = createdAt;
    }

    /**
     * Returns the id.
     */
    public UUID getId() {
        return id;
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
     * Returns the linked telegram username.
     */
    public String getLinkedTelegramUsername() {
        return linkedTelegramUsername;
    }

    /**
     * Returns the merged into participant id.
     */
    public UUID getMergedIntoParticipantId() {
        return mergedIntoParticipantId;
    }

    /**
     * Returns whether active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns the created at.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
