package ru.splitus.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public UUID getCheckId() {
        return checkId;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public List<SettlementBalance> getBalances() {
        return balances;
    }

    public Map<String, Long> getBalanceMap() {
        return new LinkedHashMap<String, Long>(balanceMap);
    }
}
