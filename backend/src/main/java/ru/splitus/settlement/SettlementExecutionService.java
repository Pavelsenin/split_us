package ru.splitus.settlement;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiException;

/**
 * Orchestrates settlement execution with basic runtime safety guarantees.
 *
 * <p>The service prevents parallel calculations for the same check and verifies
 * that the underlying data fingerprint has not changed between snapshot capture
 * and plan publication.
 */
@Service
public class SettlementExecutionService {

    private final SettlementQueryService settlementQueryService;
    private final ConcurrentHashMap<UUID, ReentrantLock> checkLocks = new ConcurrentHashMap<UUID, ReentrantLock>();

    /**
     * Creates a new settlement execution service instance.
     */
    public SettlementExecutionService(SettlementQueryService settlementQueryService) {
        this.settlementQueryService = settlementQueryService;
    }

    /**
     * Executes a stable settlement calculation for the given check.
     *
     * @param checkId target check identifier
     * @return settlement balances and transfer plan
     * @throws ApiException with {@code SETTLEMENT_ALREADY_RUNNING} when another calculation is in progress
     * @throws ApiException with {@code SETTLEMENT_STATE_CHANGED} when data changed during the calculation
     */
    public SettlementResult calculateStable(UUID checkId) {
        ReentrantLock lock = checkLocks.computeIfAbsent(checkId, id -> new ReentrantLock());
        if (!lock.tryLock()) {
            throw new ApiException(
                    ApiErrorCode.SETTLEMENT_ALREADY_RUNNING,
                    HttpStatus.CONFLICT,
                    "Settlement calculation is already running for this check"
            );
        }

        try {
            SettlementSnapshot before = settlementQueryService.loadSnapshot(checkId);
            SettlementResult result = settlementQueryService.calculate(before);
            SettlementSnapshot after = settlementQueryService.loadSnapshot(checkId);
            if (!before.getFingerprint().equals(after.getFingerprint())) {
                throw new ApiException(
                        ApiErrorCode.SETTLEMENT_STATE_CHANGED,
                        HttpStatus.CONFLICT,
                        "Settlement data changed during calculation. Retry the request."
                );
            }
            return result;
        } finally {
            lock.unlock();
            if (!lock.isLocked() && !lock.hasQueuedThreads()) {
                checkLocks.remove(checkId, lock);
            }
        }
    }
}



