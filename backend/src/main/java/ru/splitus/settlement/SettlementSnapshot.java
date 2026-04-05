package ru.splitus.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable settlement input snapshot used for stable execution.
 *
 * <p>The snapshot stores the aggregated balances together with a fingerprint
 * derived from participants, expenses, shares and timestamps.
 */
public class SettlementSnapshot {

    private final UUID checkId;
    private final String fingerprint;
    private final List<SettlementBalance> balances;
    private final Map<String, Long> balanceMap;

    public SettlementSnapshot(UUID checkId, String fingerprint, List<SettlementBalance> balances, Map<String, Long> balanceMap) {
        this.checkId = checkId;
        this.fingerprint = fingerprint;
        this.balances = Collections.unmodifiableList(new ArrayList<SettlementBalance>(balances));
        this.balanceMap = Collections.unmodifiableMap(new LinkedHashMap<String, Long>(balanceMap));
    }

    /**
     * @return check identifier this snapshot belongs to
     */
    public UUID getCheckId() {
        return checkId;
    }

    /**
     * @return deterministic fingerprint used to detect concurrent data changes
     */
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * @return immutable list of balances visible at snapshot time
     */
    public List<SettlementBalance> getBalances() {
        return balances;
    }

    /**
     * @return copy of the zero-sum balance map used by the exact solver
     */
    public Map<String, Long> getBalanceMap() {
        return new LinkedHashMap<String, Long>(balanceMap);
    }
}
