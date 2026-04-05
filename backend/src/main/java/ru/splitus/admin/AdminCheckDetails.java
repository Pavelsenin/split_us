package ru.splitus.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents admin check details.
 */
public class AdminCheckDetails {

    private final AdminCheckSummary summary;
    private final List<AdminParticipantView> participants;
    private final List<AdminParticipantMergeView> merges;
    private final List<AdminExpenseView> expenses;

    /**
     * Creates a new admin check details instance.
     */
    public AdminCheckDetails(
            AdminCheckSummary summary,
            List<AdminParticipantView> participants,
            List<AdminParticipantMergeView> merges,
            List<AdminExpenseView> expenses) {
        this.summary = summary;
        this.participants = Collections.unmodifiableList(new ArrayList<AdminParticipantView>(participants));
        this.merges = Collections.unmodifiableList(new ArrayList<AdminParticipantMergeView>(merges));
        this.expenses = Collections.unmodifiableList(new ArrayList<AdminExpenseView>(expenses));
    }

    /**
     * Returns the summary.
     */
    public AdminCheckSummary getSummary() {
        return summary;
    }

    /**
     * Returns the participants.
     */
    public List<AdminParticipantView> getParticipants() {
        return participants;
    }

    /**
     * Returns the merges.
     */
    public List<AdminParticipantMergeView> getMerges() {
        return merges;
    }

    /**
     * Returns the expenses.
     */
    public List<AdminExpenseView> getExpenses() {
        return expenses;
    }
}
