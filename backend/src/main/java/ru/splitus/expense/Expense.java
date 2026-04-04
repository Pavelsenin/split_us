package ru.splitus.expense;

import java.time.OffsetDateTime;
import java.util.UUID;

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

    public UUID getId() {
        return id;
    }

    public UUID getCheckId() {
        return checkId;
    }

    public long getAmountMinor() {
        return amountMinor;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public UUID getPayerParticipantId() {
        return payerParticipantId;
    }

    public String getComment() {
        return comment;
    }

    public String getSourceMessageText() {
        return sourceMessageText;
    }

    public Long getTelegramChatId() {
        return telegramChatId;
    }

    public Long getTelegramMessageId() {
        return telegramMessageId;
    }

    public ExpenseStatus getStatus() {
        return status;
    }

    public UUID getCreatedByParticipantId() {
        return createdByParticipantId;
    }

    public UUID getUpdatedByParticipantId() {
        return updatedByParticipantId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}

