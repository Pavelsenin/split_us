package ru.splitus.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettlementPlan {

    private final List<Transfer> transfers;

    public SettlementPlan(List<Transfer> transfers) {
        this.transfers = Collections.unmodifiableList(new ArrayList<Transfer>(transfers));
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public int size() {
        return transfers.size();
    }

    public static class Transfer {
        private final String fromParticipant;
        private final String toParticipant;
        private final long amountMinor;

        public Transfer(String fromParticipant, String toParticipant, long amountMinor) {
            this.fromParticipant = fromParticipant;
            this.toParticipant = toParticipant;
            this.amountMinor = amountMinor;
        }

        public String getFromParticipant() {
            return fromParticipant;
        }

        public String getToParticipant() {
            return toParticipant;
        }

        public long getAmountMinor() {
            return amountMinor;
        }

        public String describe() {
            return fromParticipant + "->" + toParticipant + ":" + amountMinor;
        }
    }
}

