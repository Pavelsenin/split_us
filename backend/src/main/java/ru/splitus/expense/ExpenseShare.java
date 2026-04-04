package ru.splitus.expense;

import java.util.UUID;

public class ExpenseShare {

    private final UUID expenseId;
    private final UUID participantId;
    private final long shareMinor;

    public ExpenseShare(UUID expenseId, UUID participantId, long shareMinor) {
        this.expenseId = expenseId;
        this.participantId = participantId;
        this.shareMinor = shareMinor;
    }

    public UUID getExpenseId() {
        return expenseId;
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public long getShareMinor() {
        return shareMinor;
    }
}

