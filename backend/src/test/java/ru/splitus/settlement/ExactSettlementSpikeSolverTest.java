package ru.splitus.settlement;

import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExactSettlementSpikeSolverTest {

    private final ExactSettlementSpikeSolver solver = new ExactSettlementSpikeSolver();

    @Test
    void solvesSimpleCaseInOneTransfer() {
        SettlementPlan plan = solver.solve(ExactSettlementSpikeSolver.balancesOf(
                "alice", Long.valueOf(-1_000L),
                "bob", Long.valueOf(1_000L)
        ));

        Assertions.assertEquals(1, plan.size());
        Assertions.assertEquals("alice->bob:1000", plan.getTransfers().get(0).describe());
    }

    @Test
    void findsMinimalPlanForExactPairingCase() {
        SettlementPlan plan = solver.solve(ExactSettlementSpikeSolver.balancesOf(
                "alice", Long.valueOf(-500L),
                "bob", Long.valueOf(-300L),
                "carol", Long.valueOf(500L),
                "dave", Long.valueOf(300L)
        ));

        Assertions.assertEquals(2, plan.size());
        Assertions.assertEquals(
                "alice->carol:500,bob->dave:300",
                plan.getTransfers().stream().map(SettlementPlan.Transfer::describe).collect(Collectors.joining(","))
        );
    }

    @Test
    void producesDeterministicPlanWhenSeveralOptimaExist() {
        Map<String, Long> balances = ExactSettlementSpikeSolver.balancesOf(
                "alice", Long.valueOf(-500L),
                "bob", Long.valueOf(-500L),
                "carol", Long.valueOf(500L),
                "dave", Long.valueOf(500L)
        );

        SettlementPlan first = solver.solve(balances);
        SettlementPlan second = solver.solve(balances);

        Assertions.assertEquals(
                first.getTransfers().stream().map(SettlementPlan.Transfer::describe).collect(Collectors.joining(",")),
                second.getTransfers().stream().map(SettlementPlan.Transfer::describe).collect(Collectors.joining(","))
        );
        Assertions.assertEquals(2, first.size());
    }

    @Test
    void rejectsNonZeroBalanceSum() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                solver.solve(ExactSettlementSpikeSolver.balancesOf(
                        "alice", Long.valueOf(-500L),
                        "bob", Long.valueOf(200L)
                ))
        );
    }
}

