package ru.splitus.web.dto;

import java.util.UUID;
import ru.splitus.expense.ExpenseShare;

public class ExpenseShareResponse {

    private UUID participantId;
    private long shareMinor;

    public static ExpenseShareResponse fromDomain(ExpenseShare share) {
        ExpenseShareResponse response = new ExpenseShareResponse();
        response.participantId = share.getParticipantId();
        response.shareMinor = share.getShareMinor();
        return response;
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public long getShareMinor() {
        return shareMinor;
    }
}

