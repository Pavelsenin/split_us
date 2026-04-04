package ru.splitus.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExactSettlementSpikeSolver {

    public SettlementPlan solve(Map<String, Long> participantBalances) {
        validateBalances(participantBalances);

        List<Balance> debtors = new ArrayList<Balance>();
        List<Balance> creditors = new ArrayList<Balance>();

        for (Map.Entry<String, Long> entry : participantBalances.entrySet()) {
            long balance = entry.getValue().longValue();
            if (balance < 0L) {
                debtors.add(new Balance(entry.getKey(), -balance));
            } else if (balance > 0L) {
                creditors.add(new Balance(entry.getKey(), balance));
            }
        }

        sortBalances(debtors);
        sortBalances(creditors);

        SearchState state = new SearchState();
        search(debtors, creditors, 0, new ArrayList<SettlementPlan.Transfer>(), state);
        return state.bestPlan == null ? new SettlementPlan(Collections.<SettlementPlan.Transfer>emptyList()) : state.bestPlan;
    }

    private void validateBalances(Map<String, Long> participantBalances) {
        long sum = 0L;
        for (Long value : participantBalances.values()) {
            if (value == null) {
                throw new IllegalArgumentException("Balance value must not be null");
            }
            sum += value.longValue();
        }
        if (sum != 0L) {
            throw new IllegalArgumentException("Balance sum must be zero");
        }
    }

    private void sortBalances(List<Balance> balances) {
        Collections.sort(balances, new Comparator<Balance>() {
            @Override
            public int compare(Balance left, Balance right) {
                int byName = left.participant.compareTo(right.participant);
                if (byName != 0) {
                    return byName;
                }
                return Long.compare(left.amountMinor, right.amountMinor);
            }
        });
    }

    private void search(
            List<Balance> debtors,
            List<Balance> creditors,
            int debtorIndex,
            List<SettlementPlan.Transfer> currentTransfers,
            SearchState state) {

        if (state.bestPlan != null && currentTransfers.size() >= state.bestPlan.size()) {
            return;
        }

        int nextDebtorIndex = nextNonZeroDebtor(debtors, debtorIndex);
        if (nextDebtorIndex == debtors.size()) {
            state.consider(currentTransfers);
            return;
        }

        Balance debtor = debtors.get(nextDebtorIndex);
        for (int i = 0; i < creditors.size(); i++) {
            Balance creditor = creditors.get(i);
            if (creditor.amountMinor == 0L) {
                continue;
            }

            long transferAmount = Math.min(debtor.amountMinor, creditor.amountMinor);
            debtor.amountMinor -= transferAmount;
            creditor.amountMinor -= transferAmount;
            currentTransfers.add(new SettlementPlan.Transfer(debtor.participant, creditor.participant, transferAmount));

            search(debtors, creditors, debtor.amountMinor == 0L ? nextDebtorIndex + 1 : nextDebtorIndex, currentTransfers, state);

            currentTransfers.remove(currentTransfers.size() - 1);
            creditor.amountMinor += transferAmount;
            debtor.amountMinor += transferAmount;
        }
    }

    private int nextNonZeroDebtor(List<Balance> debtors, int debtorIndex) {
        int currentIndex = debtorIndex;
        while (currentIndex < debtors.size() && debtors.get(currentIndex).amountMinor == 0L) {
            currentIndex++;
        }
        return currentIndex;
    }

    private static class Balance {
        private final String participant;
        private long amountMinor;

        private Balance(String participant, long amountMinor) {
            this.participant = participant;
            this.amountMinor = amountMinor;
        }
    }

    private static class SearchState {
        private SettlementPlan bestPlan;

        private void consider(List<SettlementPlan.Transfer> candidateTransfers) {
            SettlementPlan candidatePlan = new SettlementPlan(candidateTransfers);
            if (bestPlan == null) {
                bestPlan = candidatePlan;
                return;
            }

            if (candidatePlan.size() < bestPlan.size()) {
                bestPlan = candidatePlan;
                return;
            }

            if (candidatePlan.size() == bestPlan.size() && compare(candidatePlan, bestPlan) < 0) {
                bestPlan = candidatePlan;
            }
        }

        private int compare(SettlementPlan left, SettlementPlan right) {
            int max = Math.min(left.getTransfers().size(), right.getTransfers().size());
            for (int i = 0; i < max; i++) {
                String leftDescription = left.getTransfers().get(i).describe();
                String rightDescription = right.getTransfers().get(i).describe();
                int currentCompare = leftDescription.compareTo(rightDescription);
                if (currentCompare != 0) {
                    return currentCompare;
                }
            }
            return Integer.compare(left.getTransfers().size(), right.getTransfers().size());
        }
    }

    public static Map<String, Long> balancesOf(Object... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Expected pairs of participant and balance");
        }

        Map<String, Long> result = new LinkedHashMap<String, Long>();
        for (int i = 0; i < values.length; i += 2) {
            result.put((String) values[i], (Long) values[i + 1]);
        }
        return result;
    }
}

