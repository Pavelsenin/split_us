package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import ru.splitus.expense.ExpenseStatus;

/**
 * Represents admin expense view.
 */
public class AdminExpenseView {

    private final UUID id;
    private final long amountMinor;
    private final String currencyCode;
    private final ExpenseStatus status;
    private final String payerDisplayName;
    private final String comment;
    private final String sourceMessageText;
    private final Long telegramChatId;
    private final Long telegramMessageId;
    private final String createdByDisplayName;
    private final String updatedByDisplayName;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final List<AdminExpenseShareView> shares;

    /**
     * Creates a new admin expense view instance.
     */
    public AdminExpenseView(
            UUID id,
            long amountMinor,
            String currencyCode,
            ExpenseStatus status,
            String payerDisplayName,
            String comment,
            String sourceMessageText,
            Long telegramChatId,
            Long telegramMessageId,
            String createdByDisplayName,
            String updatedByDisplayName,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            List<AdminExpenseShareView> shares) {
        this.id = id;
        this.amountMinor = amountMinor;
        this.currencyCode = currencyCode;
        this.status = status;
        this.payerDisplayName = payerDisplayName;
        this.comment = comment;
        this.sourceMessageText = sourceMessageText;
        this.telegramChatId = telegramChatId;
        this.telegramMessageId = telegramMessageId;
        this.createdByDisplayName = createdByDisplayName;
        this.updatedByDisplayName = updatedByDisplayName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.shares = Collections.unmodifiableList(new ArrayList<AdminExpenseShareView>(shares));
    }

    /**
     * Returns the id.
     */
    public UUID getId() {
        return id;
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
     * Returns the status.
     */
    public ExpenseStatus getStatus() {
        return status;
    }

    /**
     * Returns the payer display name.
     */
    public String getPayerDisplayName() {
        return payerDisplayName;
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
     * Returns the created by display name.
     */
    public String getCreatedByDisplayName() {
        return createdByDisplayName;
    }

    /**
     * Returns the updated by display name.
     */
    public String getUpdatedByDisplayName() {
        return updatedByDisplayName;
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

    /**
     * Returns the shares.
     */
    public List<AdminExpenseShareView> getShares() {
        return shares;
    }
}
