package ru.splitus.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Final settlement payload combining participant balances and transfer plan.
 */
public class SettlementResult {

    private final List<SettlementBalance> balances;
    private final SettlementPlan plan;

    /**
     * Creates a new settlement result instance.
     */
    public SettlementResult(List<SettlementBalance> balances, SettlementPlan plan) {
        this.balances = Collections.unmodifiableList(new ArrayList<SettlementBalance>(balances));
        this.plan = plan;
    }

    /**
     * @return immutable list of participant balances used to build the plan
     */
    public List<SettlementBalance> getBalances() {
        return balances;
    }

    /**
     * @return exact transfer plan derived from the balances
     */
    public SettlementPlan getPlan() {
        return plan;
    }
}



