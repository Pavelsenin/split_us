package ru.splitus.settlement;

public class SettlementBalance {

    private final String participant;
    private final long balanceMinor;

    public SettlementBalance(String participant, long balanceMinor) {
        this.participant = participant;
        this.balanceMinor = balanceMinor;
    }

    public String getParticipant() {
        return participant;
    }

    public long getBalanceMinor() {
        return balanceMinor;
    }
}
