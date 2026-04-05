package ru.splitus.expense;

import java.util.List;
import java.util.UUID;

/**
 * Defines persistence operations for expense share.
 */
public interface ExpenseShareRepository {

    /**
     * Executes save all.
     */
    void saveAll(List<ExpenseShare> shares);

    /**
     * Finds by expense id.
     */
    List<ExpenseShare> findByExpenseId(UUID expenseId);

    /**
     * Deletes by expense id.
     */
    void deleteByExpenseId(UUID expenseId);
}



