package ru.splitus.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import ru.splitus.check.Participant;

/**
 * Represents the participant response payload.
 */
public class ParticipantResponse {

    private UUID id;
    private String type;
    private String displayName;
    private UUID linkedUserId;
    private UUID mergedIntoParticipantId;
    private OffsetDateTime createdAt;

    /**
     * Creates participant response from domain.
     */
    public static ParticipantResponse fromDomain(Participant participant) {
        ParticipantResponse response = new ParticipantResponse();
        response.id = participant.getId();
        response.type = participant.getType().name();
        response.displayName = participant.getDisplayName();
        response.linkedUserId = participant.getLinkedUserId();
        response.mergedIntoParticipantId = participant.getMergedIntoParticipantId();
        response.createdAt = participant.getCreatedAt();
        return response;
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
    public String getType() {
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
}




