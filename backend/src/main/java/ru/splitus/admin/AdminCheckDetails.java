package ru.splitus.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ru.splitus.settlement.SettlementResult;

/**
 * Full read-only admin view of a check.
 */
public class AdminCheckDetails {

    private final AdminCheckSummary summary;
    private final List<AdminParticipantView> participants;
    private final List<AdminParticipantMergeView> merges;
    private final List<AdminExpenseView> expenses;
    private final SettlementResult settlement;

    /**
     * Creates a new admin check details instance.
     */
    public AdminCheckDetails(
            AdminCheckSummary summary,
            List<AdminParticipantView> participants,
            List<AdminParticipantMergeView> merges,
            List<AdminExpenseView> expenses,
            SettlementResult settlement) {
        this.summary = summary;
        this.participants = Collections.unmodifiableList(new ArrayList<AdminParticipantView>(participants));
        this.merges = Collections.unmodifiableList(new ArrayList<AdminParticipantMergeView>(merges));
        this.expenses = Collections.unmodifiableList(new ArrayList<AdminExpenseView>(expenses));
        this.settlement = settlement;
    }

    /**
     * Returns the check summary.
     */
    public AdminCheckSummary getSummary() {
        return summary;
    }

    /**
     * Returns participants.
     */
    public List<AdminParticipantView> getParticipants() {
        return participants;
    }

    /**
     * Returns merge history.
     */
    public List<AdminParticipantMergeView> getMerges() {
        return merges;
    }

    /**
     * Returns expenses.
     */
    public List<AdminExpenseView> getExpenses() {
        return expenses;
    }

    /**
     * Returns the current settlement snapshot.
     */
    public SettlementResult getSettlement() {
        return settlement;
    }
}
