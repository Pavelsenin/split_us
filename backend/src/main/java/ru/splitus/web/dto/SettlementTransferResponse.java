package ru.splitus.web.dto;

import ru.splitus.settlement.SettlementPlan;

/**
 * API response DTO for a single settlement transfer.
 */
public class SettlementTransferResponse {

    private String fromParticipant;
    private String toParticipant;
    private long amountMinor;

    /**
     * Maps a transfer from the settlement-domain plan to the API response shape.
     *
     * @param transfer domain transfer
     * @return response DTO
     */
    public static SettlementTransferResponse fromDomain(SettlementPlan.Transfer transfer) {
        SettlementTransferResponse response = new SettlementTransferResponse();
        response.fromParticipant = transfer.getFromParticipant();
        response.toParticipant = transfer.getToParticipant();
        response.amountMinor = transfer.getAmountMinor();
        return response;
    }

    /**
     * @return sender display name
     */
    public String getFromParticipant() {
        return fromParticipant;
    }

    /**
     * @return receiver display name
     */
    public String getToParticipant() {
        return toParticipant;
    }

    /**
     * @return transfer amount in minor units
     */
    public long getAmountMinor() {
        return amountMinor;
    }
}


