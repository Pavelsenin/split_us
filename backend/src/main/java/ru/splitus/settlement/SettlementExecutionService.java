package ru.splitus.settlement;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger log = LoggerFactory.getLogger(SettlementExecutionService.class);

    private final SettlementQueryService settlementQueryService;
    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<UUID, ReentrantLock> checkLocks = new ConcurrentHashMap<UUID, ReentrantLock>();

    /**
     * Creates a new settlement execution service instance.
     */
    public SettlementExecutionService(SettlementQueryService settlementQueryService) {
        this(settlementQueryService, new SimpleMeterRegistry());
    }

    /**
     * Creates a new settlement execution service instance.
     */
    @Autowired
    public SettlementExecutionService(SettlementQueryService settlementQueryService, MeterRegistry meterRegistry) {
        this.settlementQueryService = settlementQueryService;
        this.meterRegistry = meterRegistry;
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
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        ReentrantLock lock = checkLocks.computeIfAbsent(checkId, id -> new ReentrantLock());
        if (!lock.tryLock()) {
            outcome = "already_running";
            meterRegistry.counter("splitus.settlement.conflicts.total", "reason", "already_running").increment();
            recordDuration(sample, outcome);
            log.warn("Settlement request rejected because another calculation is already running: checkId={}", checkId);
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
                outcome = "state_changed";
                meterRegistry.counter("splitus.settlement.conflicts.total", "reason", "state_changed").increment();
                log.warn("Settlement snapshot changed during calculation: checkId={}", checkId);
                throw new ApiException(
                        ApiErrorCode.SETTLEMENT_STATE_CHANGED,
                        HttpStatus.CONFLICT,
                        "Settlement data changed during calculation. Retry the request."
                );
            }
            meterRegistry.counter("splitus.settlement.requests.total", "outcome", "success").increment();
            log.info(
                    "Settlement calculated: checkId={} balances={} transfers={}",
                    checkId,
                    Integer.valueOf(result.getBalances().size()),
                    Integer.valueOf(result.getPlan().size())
            );
            return result;
        } catch (ApiException exception) {
            if ("success".equals(outcome)) {
                outcome = "api_error";
                meterRegistry.counter("splitus.settlement.requests.total", "outcome", outcome).increment();
            }
            throw exception;
        } catch (RuntimeException exception) {
            outcome = "failure";
            meterRegistry.counter("splitus.settlement.requests.total", "outcome", outcome).increment();
            log.error("Unexpected settlement failure: checkId={}", checkId, exception);
            throw exception;
        } finally {
            recordDuration(sample, outcome);
            lock.unlock();
            if (!lock.isLocked() && !lock.hasQueuedThreads()) {
                checkLocks.remove(checkId, lock);
            }
        }
    }

    private void recordDuration(Timer.Sample sample, String outcome) {
        sample.stop(Timer.builder("splitus.settlement.duration")
                .description("Stable settlement execution duration")
                .tag("outcome", outcome)
                .publishPercentileHistogram()
                .register(meterRegistry));
    }
}

