package ru.splitus.web.dto;

import ru.splitus.settlement.SettlementPlan;

public class SettlementTransferResponse {

    private String fromParticipant;
    private String toParticipant;
    private long amountMinor;

    public static SettlementTransferResponse fromDomain(SettlementPlan.Transfer transfer) {
        SettlementTransferResponse response = new SettlementTransferResponse();
        response.fromParticipant = transfer.getFromParticipant();
        response.toParticipant = transfer.getToParticipant();
        response.amountMinor = transfer.getAmountMinor();
        return response;
    }

    public String getFromParticipant() {
        return fromParticipant;
    }

    public String getToParticipant() {
        return toParticipant;
    }

    public long getAmountMinor() {
        return amountMinor;
    }
}
