package ru.splitus.expense;

import java.util.List;
import java.util.UUID;

public interface ExpenseShareRepository {

    void saveAll(List<ExpenseShare> shares);

    List<ExpenseShare> findByExpenseId(UUID expenseId);

    void deleteByExpenseId(UUID expenseId);
}
