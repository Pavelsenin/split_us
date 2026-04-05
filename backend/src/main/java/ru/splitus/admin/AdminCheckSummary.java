package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Compact admin projection of a check shown on the dashboard and detail header.
 */
public class AdminCheckSummary {

    private final UUID checkId;
    private final String title;
    private final String inviteToken;
    private final String currencyCode;
    private final Long telegramChatId;
    private final boolean chatActive;
    private final long ownerTelegramUserId;
    private final String ownerTelegramUsername;
    private final int activeParticipantCount;
    private final int expenseCount;
    private final OffsetDateTime createdAt;

    /**
     * Creates a new admin check summary instance.
     */
    public AdminCheckSummary(
            UUID checkId,
            String title,
            String inviteToken,
            String currencyCode,
            Long telegramChatId,
            boolean chatActive,
            long ownerTelegramUserId,
            String ownerTelegramUsername,
            int activeParticipantCount,
            int expenseCount,
            OffsetDateTime createdAt) {
        this.checkId = checkId;
        this.title = title;
        this.inviteToken = inviteToken;
        this.currencyCode = currencyCode;
        this.telegramChatId = telegramChatId;
        this.chatActive = chatActive;
        this.ownerTelegramUserId = ownerTelegramUserId;
        this.ownerTelegramUsername = ownerTelegramUsername;
        this.activeParticipantCount = activeParticipantCount;
        this.expenseCount = expenseCount;
        this.createdAt = createdAt;
    }

    /**
     * Returns the check id.
     */
    public UUID getCheckId() {
        return checkId;
    }

    /**
     * Returns the check title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the invite token.
     */
    public String getInviteToken() {
        return inviteToken;
    }

    /**
     * Returns the currency code.
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Returns the Telegram chat id when linked.
     */
    public Long getTelegramChatId() {
        return telegramChatId;
    }

    /**
     * Returns whether the linked Telegram chat is active.
     */
    public boolean isChatActive() {
        return chatActive;
    }

    /**
     * Returns the owner Telegram user id.
     */
    public long getOwnerTelegramUserId() {
        return ownerTelegramUserId;
    }

    /**
     * Returns the owner Telegram username.
     */
    public String getOwnerTelegramUsername() {
        return ownerTelegramUsername;
    }

    /**
     * Returns the number of active participants.
     */
    public int getActiveParticipantCount() {
        return activeParticipantCount;
    }

    /**
     * Returns the number of expenses.
     */
    public int getExpenseCount() {
        return expenseCount;
    }

    /**
     * Returns the creation timestamp.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
