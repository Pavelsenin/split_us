package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CheckBook {

    private final UUID id;
    private final String title;
    private final UUID ownerUserId;
    private final String inviteToken;
    private final Long telegramChatId;
    private final String currencyCode;
    private final boolean chatActive;
    private final OffsetDateTime createdAt;

    public CheckBook(UUID id, String title, UUID ownerUserId, Long telegramChatId, String currencyCode, boolean chatActive, OffsetDateTime createdAt) {
        this(id, title, ownerUserId, null, telegramChatId, currencyCode, chatActive, createdAt);
    }

    public CheckBook(
            UUID id,
            String title,
            UUID ownerUserId,
            String inviteToken,
            Long telegramChatId,
            String currencyCode,
            boolean chatActive,
            OffsetDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.ownerUserId = ownerUserId;
        this.inviteToken = inviteToken;
        this.telegramChatId = telegramChatId;
        this.currencyCode = currencyCode;
        this.chatActive = chatActive;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public String getInviteToken() {
        return inviteToken;
    }

    public Long getTelegramChatId() {
        return telegramChatId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public boolean isChatActive() {
        return chatActive;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
