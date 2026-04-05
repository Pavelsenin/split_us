package ru.splitus.settlement;

/**
 * Single participant balance produced before the settlement plan is calculated.
 */
public class SettlementBalance {

    private final String participant;
    private final long balanceMinor;

    public SettlementBalance(String participant, long balanceMinor) {
        this.participant = participant;
        this.balanceMinor = balanceMinor;
    }

    /**
     * @return participant display name used in the settlement result
     */
    public String getParticipant() {
        return participant;
    }

    /**
     * @return signed balance in minor units; positive means participant should receive money
     */
    public long getBalanceMinor() {
        return balanceMinor;
    }
}
