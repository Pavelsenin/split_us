package ru.splitus.expense;

import java.util.UUID;

/**
 * Represents expense share.
 */
public class ExpenseShare {

    private final UUID expenseId;
    private final UUID participantId;
    private final long shareMinor;

    /**
     * Creates a new expense share instance.
     */
    public ExpenseShare(UUID expenseId, UUID participantId, long shareMinor) {
        this.expenseId = expenseId;
        this.participantId = participantId;
        this.shareMinor = shareMinor;
    }

    /**
     * Returns the expense id.
     */
    public UUID getExpenseId() {
        return expenseId;
    }

    /**
     * Returns the participant id.
     */
    public UUID getParticipantId() {
        return participantId;
    }

    /**
     * Returns the share minor.
     */
    public long getShareMinor() {
        return shareMinor;
    }
}




