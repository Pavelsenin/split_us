package ru.splitus.web.dto;

import ru.splitus.settlement.SettlementBalance;

/**
 * API response DTO for a single participant balance in settlement output.
 */
public class SettlementBalanceResponse {

    private String participant;
    private long balanceMinor;

    /**
     * Maps a settlement-domain balance to the API response shape.
     *
     * @param balance domain balance
     * @return response DTO
     */
    public static SettlementBalanceResponse fromDomain(SettlementBalance balance) {
        SettlementBalanceResponse response = new SettlementBalanceResponse();
        response.participant = balance.getParticipant();
        response.balanceMinor = balance.getBalanceMinor();
        return response;
    }

    /**
     * @return participant display name
     */
    public String getParticipant() {
        return participant;
    }

    /**
     * @return signed balance in minor units
     */
    public long getBalanceMinor() {
        return balanceMinor;
    }
}


