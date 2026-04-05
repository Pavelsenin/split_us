package ru.splitus.web.dto;

import ru.splitus.settlement.SettlementBalance;

public class SettlementBalanceResponse {

    private String participant;
    private long balanceMinor;

    public static SettlementBalanceResponse fromDomain(SettlementBalance balance) {
        SettlementBalanceResponse response = new SettlementBalanceResponse();
        response.participant = balance.getParticipant();
        response.balanceMinor = balance.getBalanceMinor();
        return response;
    }

    public String getParticipant() {
        return participant;
    }

    public long getBalanceMinor() {
        return balanceMinor;
    }
}
