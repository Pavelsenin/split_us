package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import ru.splitus.expense.ExpenseStatus;

/**
 * Read-only admin projection of an expense with its shares.
 */
public class AdminExpenseView {

    private final UUID expenseId;
    private final long amountMinor;
    private final ExpenseStatus status;
    private final String payerDisplayName;
    private final String createdByDisplayName;
    private final String updatedByDisplayName;
    private final String comment;
    private final String sourceMessageText;
    private final Long telegramChatId;
    private final Long telegramMessageId;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final List<AdminExpenseShareView> shares;

    /**
     * Creates a new admin expense view instance.
     */
    public AdminExpenseView(
            UUID expenseId,
            long amountMinor,
            ExpenseStatus status,
            String payerDisplayName,
            String createdByDisplayName,
            String updatedByDisplayName,
            String comment,
            String sourceMessageText,
            Long telegramChatId,
            Long telegramMessageId,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            List<AdminExpenseShareView> shares) {
        this.expenseId = expenseId;
        this.amountMinor = amountMinor;
        this.status = status;
        this.payerDisplayName = payerDisplayName;
        this.createdByDisplayName = createdByDisplayName;
        this.updatedByDisplayName = updatedByDisplayName;
        this.comment = comment;
        this.sourceMessageText = sourceMessageText;
        this.telegramChatId = telegramChatId;
        this.telegramMessageId = telegramMessageId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.shares = Collections.unmodifiableList(new ArrayList<AdminExpenseShareView>(shares));
    }

    /**
     * Returns the expense id.
     */
    public UUID getExpenseId() {
        return expenseId;
    }

    /**
     * Returns the amount in minor units.
     */
    public long getAmountMinor() {
        return amountMinor;
    }

    /**
     * Returns the expense status.
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
     * Returns the creator display name.
     */
    public String getCreatedByDisplayName() {
        return createdByDisplayName;
    }

    /**
     * Returns the last updater display name.
     */
    public String getUpdatedByDisplayName() {
        return updatedByDisplayName;
    }

    /**
     * Returns the optional comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns the raw Telegram source message text when present.
     */
    public String getSourceMessageText() {
        return sourceMessageText;
    }

    /**
     * Returns the Telegram chat id when present.
     */
    public Long getTelegramChatId() {
        return telegramChatId;
    }

    /**
     * Returns the Telegram message id when present.
     */
    public Long getTelegramMessageId() {
        return telegramMessageId;
    }

    /**
     * Returns the creation timestamp.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the last update timestamp.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Returns expense shares.
     */
    public List<AdminExpenseShareView> getShares() {
        return shares;
    }
}
