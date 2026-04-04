package ru.splitus.web.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import ru.splitus.check.CheckSnapshot;

public class CheckResponse {

    private UUID id;
    private String title;
    private String currencyCode;
    private UUID ownerUserId;
    private Long telegramChatId;
    private boolean chatActive;
    private OffsetDateTime createdAt;
    private List<ParticipantResponse> participants;

    public static CheckResponse fromDomain(CheckSnapshot snapshot) {
        CheckResponse response = new CheckResponse();
        response.id = snapshot.getCheckBook().getId();
        response.title = snapshot.getCheckBook().getTitle();
        response.currencyCode = snapshot.getCheckBook().getCurrencyCode();
        response.ownerUserId = snapshot.getCheckBook().getOwnerUserId();
        response.telegramChatId = snapshot.getCheckBook().getTelegramChatId();
        response.chatActive = snapshot.getCheckBook().isChatActive();
        response.createdAt = snapshot.getCheckBook().getCreatedAt();
        response.participants = snapshot.getParticipants().stream()
                .map(ParticipantResponse::fromDomain)
                .collect(Collectors.toList());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public Long getTelegramChatId() {
        return telegramChatId;
    }

    public boolean isChatActive() {
        return chatActive;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<ParticipantResponse> getParticipants() {
        return participants;
    }
}

