package ru.splitus.expense;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository {

    Expense save(Expense expense);

    Expense update(Expense expense);

    Optional<Expense> findById(UUID expenseId);

    Optional<Expense> findByTelegramMessage(long telegramChatId, long telegramMessageId);

    List<Expense> findByCheckId(UUID checkId);

    void deleteById(UUID expenseId);
}
