package ru.splitus.expense;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents expense.
 */
public class Expense {

    private final UUID id;
    private final UUID checkId;
    private final long amountMinor;
    private final String currencyCode;
    private final UUID payerParticipantId;
    private final String comment;
    private final String sourceMessageText;
    private final Long telegramChatId;
    private final Long telegramMessageId;
    private final ExpenseStatus status;
    private final UUID createdByParticipantId;
    private final UUID updatedByParticipantId;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    /**
     * Creates a new expense instance.
     */
    public Expense(
            UUID id,
            UUID checkId,
            long amountMinor,
            String currencyCode,
            UUID payerParticipantId,
            String comment,
            String sourceMessageText,
            Long telegramChatId,
            Long telegramMessageId,
            ExpenseStatus status,
            UUID createdByParticipantId,
            UUID updatedByParticipantId,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
        this.id = id;
        this.checkId = checkId;
        this.amountMinor = amountMinor;
        this.currencyCode = currencyCode;
        this.payerParticipantId = payerParticipantId;
        this.comment = comment;
        this.sourceMessageText = sourceMessageText;
        this.telegramChatId = telegramChatId;
        this.telegramMessageId = telegramMessageId;
        this.status = status;
        this.createdByParticipantId = createdByParticipantId;
        this.updatedByParticipantId = updatedByParticipantId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the check id.
     */
    public UUID getCheckId() {
        return checkId;
    }

    /**
     * Returns the amount minor.
     */
    public long getAmountMinor() {
        return amountMinor;
    }

    /**
     * Returns the currency code.
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Returns the payer participant id.
     */
    public UUID getPayerParticipantId() {
        return payerParticipantId;
    }

    /**
     * Returns the comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns the source message text.
     */
    public String getSourceMessageText() {
        return sourceMessageText;
    }

    /**
     * Returns the telegram chat id.
     */
    public Long getTelegramChatId() {
        return telegramChatId;
    }

    /**
     * Returns the telegram message id.
     */
    public Long getTelegramMessageId() {
        return telegramMessageId;
    }

    /**
     * Returns the status.
     */
    public ExpenseStatus getStatus() {
        return status;
    }

    /**
     * Returns the created by participant id.
     */
    public UUID getCreatedByParticipantId() {
        return createdByParticipantId;
    }

    /**
     * Returns the updated by participant id.
     */
    public UUID getUpdatedByParticipantId() {
        return updatedByParticipantId;
    }

    /**
     * Returns the created at.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the updated at.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}




