package ru.splitus.web.dto;

import java.util.UUID;
import ru.splitus.expense.ExpenseShare;

/**
 * Represents the expense share response payload.
 */
public class ExpenseShareResponse {

    private UUID participantId;
    private long shareMinor;

    /**
     * Creates expense share response from domain.
     */
    public static ExpenseShareResponse fromDomain(ExpenseShare share) {
        ExpenseShareResponse response = new ExpenseShareResponse();
        response.participantId = share.getParticipantId();
        response.shareMinor = share.getShareMinor();
        return response;
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




