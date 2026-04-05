package ru.splitus.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable transfer plan produced by the settlement solver.
 */
public class SettlementPlan {

    private final List<Transfer> transfers;

    /**
     * Creates a new settlement plan instance.
     */
    public SettlementPlan(List<Transfer> transfers) {
        this.transfers = Collections.unmodifiableList(new ArrayList<Transfer>(transfers));
    }

    /**
     * @return ordered immutable list of transfers in the final settlement plan
     */
    public List<Transfer> getTransfers() {
        return transfers;
    }

    /**
     * @return number of transfers in the plan
     */
    public int size() {
        return transfers.size();
    }

    /**
     * Single money transfer instruction inside a settlement plan.
     */
    public static class Transfer {
        private final String fromParticipant;
        private final String toParticipant;
        private final long amountMinor;

        /**
         * Creates a new transfer instance.
         */
        public Transfer(String fromParticipant, String toParticipant, long amountMinor) {
            this.fromParticipant = fromParticipant;
            this.toParticipant = toParticipant;
            this.amountMinor = amountMinor;
        }

        /**
         * @return participant who should send the money
         */
        public String getFromParticipant() {
            return fromParticipant;
        }

        /**
         * @return participant who should receive the money
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

        /**
         * @return compact deterministic textual representation used in tests and comparisons
         */
        public String describe() {
            return fromParticipant + "->" + toParticipant + ":" + amountMinor;
        }
    }
}



