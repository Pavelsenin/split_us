package ru.splitus.expense;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines persistence operations for expense.
 */
public interface ExpenseRepository {

    /**
     * Executes save.
     */
    Expense save(Expense expense);

    /**
     * Executes update.
     */
    Expense update(Expense expense);

    /**
     * Finds by id.
     */
    Optional<Expense> findById(UUID expenseId);

    /**
     * Finds by telegram message.
     */
    Optional<Expense> findByTelegramMessage(long telegramChatId, long telegramMessageId);

    /**
     * Finds by check id.
     */
    List<Expense> findByCheckId(UUID checkId);

    /**
     * Deletes by id.
     */
    void deleteById(UUID expenseId);
}



