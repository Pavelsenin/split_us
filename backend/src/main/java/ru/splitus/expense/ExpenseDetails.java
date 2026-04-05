package ru.splitus.expense;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents expense details.
 */
public class ExpenseDetails {

    private final Expense expense;
    private final List<ExpenseShare> shares;

    /**
     * Creates a new expense details instance.
     */
    public ExpenseDetails(Expense expense, List<ExpenseShare> shares) {
        this.expense = expense;
        this.shares = Collections.unmodifiableList(new ArrayList<ExpenseShare>(shares));
    }

    /**
     * Returns the expense.
     */
    public Expense getExpense() {
        return expense;
    }

    /**
     * Returns the shares.
     */
    public List<ExpenseShare> getShares() {
        return shares;
    }
}




