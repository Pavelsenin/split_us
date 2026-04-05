package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents admin check summary.
 */
public class AdminCheckSummary {

    private final UUID id;
    private final String title;
    private final String ownerTelegramUsername;
    private final String inviteToken;
    private final Long telegramChatId;
    private final String currencyCode;
    private final boolean chatActive;
    private final int activeParticipantCount;
    private final int expenseCount;
    private final OffsetDateTime createdAt;

    /**
     * Creates a new admin check summary instance.
     */
    public AdminCheckSummary(
            UUID id,
            String title,
            String ownerTelegramUsername,
            String inviteToken,
            Long telegramChatId,
            String currencyCode,
            boolean chatActive,
            int activeParticipantCount,
            int expenseCount,
            OffsetDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.ownerTelegramUsername = ownerTelegramUsername;
        this.inviteToken = inviteToken;
        this.telegramChatId = telegramChatId;
        this.currencyCode = currencyCode;
        this.chatActive = chatActive;
        this.activeParticipantCount = activeParticipantCount;
        this.expenseCount = expenseCount;
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
     * Returns the owner telegram username.
     */
    public String getOwnerTelegramUsername() {
        return ownerTelegramUsername;
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
     * Returns the active participant count.
     */
    public int getActiveParticipantCount() {
        return activeParticipantCount;
    }

    /**
     * Returns the expense count.
     */
    public int getExpenseCount() {
        return expenseCount;
    }

    /**
     * Returns the created at.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
