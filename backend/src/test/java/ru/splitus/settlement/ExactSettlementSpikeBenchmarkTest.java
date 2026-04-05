package ru.splitus.settlement;

import org.junit.jupiter.api.Test;

/**
 * Tests exact settlement spike benchmark.
 */
class ExactSettlementSpikeBenchmarkTest {

    private final ExactSettlementSpikeSolver solver = new ExactSettlementSpikeSolver();

    @Test
    void printsSmokeBenchmarkForReview() {
        runCase("case-6", ExactSettlementSpikeSolver.balancesOf(
                "a", Long.valueOf(-900L),
                "b", Long.valueOf(-300L),
                "c", Long.valueOf(-200L),
                "d", Long.valueOf(700L),
                "e", Long.valueOf(400L),
                "f", Long.valueOf(300L)
        ));

        runCase("case-8", ExactSettlementSpikeSolver.balancesOf(
                "a", Long.valueOf(-700L),
                "b", Long.valueOf(-500L),
                "c", Long.valueOf(-300L),
                "d", Long.valueOf(-100L),
                "e", Long.valueOf(400L),
                "f", Long.valueOf(400L),
                "g", Long.valueOf(400L),
                "h", Long.valueOf(400L)
        ));
    }

    private void runCase(String label, java.util.Map<String, Long> balances) {
        long startedAt = System.nanoTime();
        SettlementPlan plan = solver.solve(balances);
        long durationMicros = (System.nanoTime() - startedAt) / 1_000L;
        System.out.println(label + " transfers=" + plan.size() + " durationMicros=" + durationMicros);
    }
}



