package ru.splitus.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettlementResult {

    private final List<SettlementBalance> balances;
    private final SettlementPlan plan;

    public SettlementResult(List<SettlementBalance> balances, SettlementPlan plan) {
        this.balances = Collections.unmodifiableList(new ArrayList<SettlementBalance>(balances));
        this.plan = plan;
    }

    public List<SettlementBalance> getBalances() {
        return balances;
    }

    public SettlementPlan getPlan() {
        return plan;
    }
}
