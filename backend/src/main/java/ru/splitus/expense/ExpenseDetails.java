package ru.splitus.expense;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExpenseDetails {

    private final Expense expense;
    private final List<ExpenseShare> shares;

    public ExpenseDetails(Expense expense, List<ExpenseShare> shares) {
        this.expense = expense;
        this.shares = Collections.unmodifiableList(new ArrayList<ExpenseShare>(shares));
    }

    public Expense getExpense() {
        return expense;
    }

    public List<ExpenseShare> getShares() {
        return shares;
    }
}

