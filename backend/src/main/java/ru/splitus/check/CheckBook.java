package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents check book.
 */
public class CheckBook {

    private final UUID id;
    private final String title;
    private final UUID ownerUserId;
    private final String inviteToken;
    private final Long telegramChatId;
    private final String currencyCode;
    private final boolean chatActive;
    private final OffsetDateTime createdAt;

    /**
     * Creates a new check book instance.
     */
    public CheckBook(UUID id, String title, UUID ownerUserId, Long telegramChatId, String currencyCode, boolean chatActive, OffsetDateTime createdAt) {
        this(id, title, ownerUserId, null, telegramChatId, currencyCode, chatActive, createdAt);
    }

    /**
     * Creates a new check book instance.
     */
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
     * Returns the currency code.
     */
    public String getCurrencyCode() {
        return currencyCode;
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
}



