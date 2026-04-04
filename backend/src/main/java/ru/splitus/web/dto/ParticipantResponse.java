package ru.splitus.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import ru.splitus.check.Participant;

public class ParticipantResponse {

    private UUID id;
    private String type;
    private String displayName;
    private UUID linkedUserId;
    private UUID mergedIntoParticipantId;
    private OffsetDateTime createdAt;

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

    public UUID getId() {
        return id;
    }

    public String getType() {
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
}

