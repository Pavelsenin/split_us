package ru.splitus.web.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import ru.splitus.check.CheckSnapshot;

/**
 * Represents the check response payload.
 */
public class CheckResponse {

    private UUID id;
    private String title;
    private String currencyCode;
    private UUID ownerUserId;
    private String inviteToken;
    private Long telegramChatId;
    private boolean chatActive;
    private OffsetDateTime createdAt;
    private List<ParticipantResponse> participants;

    /**
     * Creates check response from domain.
     */
    public static CheckResponse fromDomain(CheckSnapshot snapshot) {
        CheckResponse response = new CheckResponse();
        response.id = snapshot.getCheckBook().getId();
        response.title = snapshot.getCheckBook().getTitle();
        response.currencyCode = snapshot.getCheckBook().getCurrencyCode();
        response.ownerUserId = snapshot.getCheckBook().getOwnerUserId();
        response.inviteToken = snapshot.getCheckBook().getInviteToken();
        response.telegramChatId = snapshot.getCheckBook().getTelegramChatId();
        response.chatActive = snapshot.getCheckBook().isChatActive();
        response.createdAt = snapshot.getCheckBook().getCreatedAt();
        response.participants = snapshot.getParticipants().stream()
                .map(ParticipantResponse::fromDomain)
                .collect(Collectors.toList());
        return response;
    }

    /**
     * Returns the id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the currency code.
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Returns the owner user id.
     */
    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * Returns the invite token.
     */
    public String getInviteToken() {
        return inviteToken;
    }

    /**
     * Returns the telegram chat id.
     */
    public Long getTelegramChatId() {
        return telegramChatId;
    }

    /**
     * Returns whether chat active.
     */
    public boolean isChatActive() {
        return chatActive;
    }

    /**
     * Returns the created at.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the participants.
     */
    public List<ParticipantResponse> getParticipants() {
        return participants;
    }
}



