package ru.splitus.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.splitus.check.CheckBookRepository;
import ru.splitus.check.Participant;
import ru.splitus.check.ParticipantRepository;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiException;
import ru.splitus.expense.Expense;
import ru.splitus.expense.ExpenseRepository;
import ru.splitus.expense.ExpenseShare;
import ru.splitus.expense.ExpenseShareRepository;
import ru.splitus.expense.ExpenseStatus;

@Service
public class SettlementQueryService {

    private final CheckBookRepository checkBookRepository;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final ExactSettlementSpikeSolver exactSettlementSpikeSolver;

    public SettlementQueryService(
            CheckBookRepository checkBookRepository,
            ParticipantRepository participantRepository,
            ExpenseRepository expenseRepository,
            ExpenseShareRepository expenseShareRepository,
            ExactSettlementSpikeSolver exactSettlementSpikeSolver) {
        this.checkBookRepository = checkBookRepository;
        this.participantRepository = participantRepository;
        this.expenseRepository = expenseRepository;
        this.expenseShareRepository = expenseShareRepository;
        this.exactSettlementSpikeSolver = exactSettlementSpikeSolver;
    }

    @Transactional(readOnly = true)
    public SettlementResult calculate(UUID checkId) {
        if (!checkBookRepository.findById(checkId).isPresent()) {
            throw new ApiException(ApiErrorCode.CHECK_NOT_FOUND, HttpStatus.NOT_FOUND, "Check not found");
        }

        Map<UUID, Participant> activeParticipants = activeParticipantMap(checkId);
        Map<String, Long> balances = new LinkedHashMap<String, Long>();
        for (Participant participant : orderedParticipants(activeParticipants)) {
            balances.put(participant.getDisplayName(), Long.valueOf(0L));
        }

        List<Expense> expenses = expenseRepository.findByCheckId(checkId);
        for (Expense expense : expenses) {
            if (expense.getStatus() != ExpenseStatus.VALID) {
                continue;
            }

            Participant payer = activeParticipants.get(expense.getPayerParticipantId());
            if (payer != null) {
                increment(balances, payer.getDisplayName(), expense.getAmountMinor());
            }

            List<ExpenseShare> shares = expenseShareRepository.findByExpenseId(expense.getId());
            for (ExpenseShare share : shares) {
                Participant participant = activeParticipants.get(share.getParticipantId());
                if (participant != null) {
                    increment(balances, participant.getDisplayName(), -share.getShareMinor());
                }
            }
        }

        List<SettlementBalance> settlementBalances = new ArrayList<SettlementBalance>();
        for (Map.Entry<String, Long> entry : balances.entrySet()) {
            settlementBalances.add(new SettlementBalance(entry.getKey(), entry.getValue().longValue()));
        }
        SettlementPlan plan = exactSettlementSpikeSolver.solve(balances);
        return new SettlementResult(settlementBalances, plan);
    }

    private Map<UUID, Participant> activeParticipantMap(UUID checkId) {
        List<Participant> participants = participantRepository.findByCheckId(checkId);
        Map<UUID, Participant> result = new LinkedHashMap<UUID, Participant>();
        for (Participant participant : participants) {
            if (participant.isActive()) {
                result.put(participant.getId(), participant);
            }
        }
        return result;
    }

    private List<Participant> orderedParticipants(Map<UUID, Participant> activeParticipants) {
        List<Participant> participants = new ArrayList<Participant>(activeParticipants.values());
        Collections.sort(participants, (left, right) -> left.getDisplayName().compareTo(right.getDisplayName()));
        return participants;
    }

    private void increment(Map<String, Long> balances, String participant, long delta) {
        Long current = balances.get(participant);
        balances.put(participant, Long.valueOf((current == null ? 0L : current.longValue()) + delta));
    }
}
