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

/**
 * Read-only settlement data service.
 *
 * <p>This service aggregates valid expenses into participant balances and can
 * either return a full immutable snapshot or calculate a plan directly from it.
 */
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

    /**
     * Calculates settlement directly from the current persisted state.
     *
     * @param checkId target check identifier
     * @return balances and exact transfer plan
     */
    @Transactional(readOnly = true)
    public SettlementResult calculate(UUID checkId) {
        return calculate(loadSnapshot(checkId));
    }

    /**
     * Calculates settlement from a previously captured immutable snapshot.
     *
     * @param snapshot precomputed settlement snapshot
     * @return balances and exact transfer plan
     */
    public SettlementResult calculate(SettlementSnapshot snapshot) {
        return new SettlementResult(snapshot.getBalances(), exactSettlementSpikeSolver.solve(snapshot.getBalanceMap()));
    }

    /**
     * Captures a deterministic snapshot of all active participants and valid expenses
     * relevant for settlement calculation.
     *
     * @param checkId target check identifier
     * @return snapshot including balances and a fingerprint for change detection
     */
    @Transactional(readOnly = true)
    public SettlementSnapshot loadSnapshot(UUID checkId) {
        if (!checkBookRepository.findById(checkId).isPresent()) {
            throw new ApiException(ApiErrorCode.CHECK_NOT_FOUND, HttpStatus.NOT_FOUND, "Check not found");
        }

        Map<UUID, Participant> activeParticipants = activeParticipantMap(checkId);
        List<Participant> orderedParticipants = orderedParticipants(activeParticipants);
        Map<String, Long> balances = new LinkedHashMap<String, Long>();
        StringBuilder fingerprint = new StringBuilder();
        fingerprint.append("check=").append(checkId);
        for (Participant participant : orderedParticipants) {
            balances.put(participant.getDisplayName(), Long.valueOf(0L));
            fingerprint.append("|participant:")
                    .append(participant.getId())
                    .append(':')
                    .append(participant.getDisplayName())
                    .append(':')
                    .append(participant.getType().name())
                    .append(':')
                    .append(participant.getCreatedAt());
        }

        List<Expense> expenses = expenseRepository.findByCheckId(checkId);
        for (Expense expense : expenses) {
            List<ExpenseShare> shares = orderedShares(expenseShareRepository.findByExpenseId(expense.getId()));
            fingerprint.append("|expense:")
                    .append(expense.getId())
                    .append(':')
                    .append(expense.getStatus().name())
                    .append(':')
                    .append(expense.getAmountMinor())
                    .append(':')
                    .append(expense.getPayerParticipantId())
                    .append(':')
                    .append(expense.getUpdatedAt());
            for (ExpenseShare share : shares) {
                fingerprint.append("|share:")
                        .append(share.getExpenseId())
                        .append(':')
                        .append(share.getParticipantId())
                        .append(':')
                        .append(share.getShareMinor());
            }

            if (expense.getStatus() != ExpenseStatus.VALID) {
                continue;
            }

            Participant payer = activeParticipants.get(expense.getPayerParticipantId());
            if (payer != null) {
                increment(balances, payer.getDisplayName(), expense.getAmountMinor());
            }

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
        return new SettlementSnapshot(checkId, fingerprint.toString(), settlementBalances, balances);
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

    private List<ExpenseShare> orderedShares(List<ExpenseShare> shares) {
        List<ExpenseShare> orderedShares = new ArrayList<ExpenseShare>(shares);
        Collections.sort(orderedShares, (left, right) -> left.getParticipantId().toString().compareTo(right.getParticipantId().toString()));
        return orderedShares;
    }

    private void increment(Map<String, Long> balances, String participant, long delta) {
        Long current = balances.get(participant);
        balances.put(participant, Long.valueOf((current == null ? 0L : current.longValue()) + delta));
    }
}
