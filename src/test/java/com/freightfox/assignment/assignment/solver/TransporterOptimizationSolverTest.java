
package com.freightfox.assignment.assignment.solver;

import com.freightfox.assignment.exception.OptimizationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TransporterOptimizationSolverTest {

    private TransporterOptimizationSolver solver;

    @BeforeEach
    void setUp() {
        solver = new TransporterOptimizationSolver();
    }

    @Test
    void shouldMinimizeTotalAssignmentCost() {
        OptimizationInput input = new OptimizationInput(
                List.of(
                        new SolverLane(1L),
                        new SolverLane(2L)
                ),
                List.of(
                        new SolverTransporter(10L),
                        new SolverTransporter(20L)
                ),
                List.of(
                        new SolverQuote(1L, 10L, new BigDecimal("100")),
                        new SolverQuote(1L, 20L, new BigDecimal("500")),
                        new SolverQuote(2L, 10L, new BigDecimal("500")),
                        new SolverQuote(2L, 20L, new BigDecimal("100"))
                ),
                2
        );

        OptimizationResult result = solver.solve(input);

        assertEquals("OPTIMAL", result.solverStatus());
        assertEquals(0, new BigDecimal("200").compareTo(result.totalCost()));
        assertEquals(2, result.assignments().size());
        assertEquals(2, result.selectedTransporters().size());
    }

    @Test
    void shouldMaximizeDistinctTransportersWithinLimit() {
        OptimizationInput input = new OptimizationInput(
                List.of(
                        new SolverLane(1L),
                        new SolverLane(2L),
                        new SolverLane(3L)
                ),
                List.of(
                        new SolverTransporter(10L),
                        new SolverTransporter(20L),
                        new SolverTransporter(30L)
                ),
                List.of(
                        new SolverQuote(1L, 10L, new BigDecimal("100")),
                        new SolverQuote(1L, 20L, new BigDecimal("200")),
                        new SolverQuote(2L, 10L, new BigDecimal("100")),
                        new SolverQuote(2L, 30L, new BigDecimal("200")),
                        new SolverQuote(3L, 10L, new BigDecimal("100")),
                        new SolverQuote(3L, 30L, new BigDecimal("200"))
                ),
                3
        );

        OptimizationResult result = solver.solve(input);

        assertEquals("OPTIMAL", result.solverStatus());
        assertEquals(3, result.assignments().size());
        assertEquals(3, result.selectedTransporters().size());

        assertEquals(
                Set.of(10L, 20L, 30L),
                Set.copyOf(result.selectedTransporters())
        );
    }

    @Test
    void shouldRespectMaximumTransporterLimit() {
        OptimizationInput input = new OptimizationInput(
                List.of(
                        new SolverLane(1L),
                        new SolverLane(2L),
                        new SolverLane(3L)
                ),
                List.of(
                        new SolverTransporter(10L),
                        new SolverTransporter(20L),
                        new SolverTransporter(30L)
                ),
                List.of(
                        new SolverQuote(1L, 10L, new BigDecimal("100")),
                        new SolverQuote(1L, 20L, new BigDecimal("200")),
                        new SolverQuote(2L, 10L, new BigDecimal("100")),
                        new SolverQuote(2L, 20L, new BigDecimal("200")),
                        new SolverQuote(3L, 10L, new BigDecimal("100")),
                        new SolverQuote(3L, 30L, new BigDecimal("200"))
                ),
                2
        );

        OptimizationResult result = solver.solve(input);

        assertEquals("OPTIMAL", result.solverStatus());
        assertTrue(result.selectedTransporters().size() <= 2);
        assertEquals(3, result.assignments().size());
    }

    @Test
    void shouldPrioritizeTransporterUtilizationOverCheapestCost() {
        OptimizationInput input = new OptimizationInput(
                List.of(
                        new SolverLane(1L),
                        new SolverLane(2L)
                ),
                List.of(
                        new SolverTransporter(10L),
                        new SolverTransporter(20L),
                        new SolverTransporter(30L)
                ),
                List.of(
                        new SolverQuote(1L, 10L, new BigDecimal("10")),
                        new SolverQuote(1L, 20L, new BigDecimal("30")),
                        new SolverQuote(2L, 10L, new BigDecimal("10")),
                        new SolverQuote(2L, 30L, new BigDecimal("40"))
                ),
                2
        );

        OptimizationResult result = solver.solve(input);

        assertEquals("OPTIMAL", result.solverStatus());

        // Both lanes must be assigned.
        assertEquals(2, result.assignments().size());

        // Two distinct transporters must be used,
        // even though transporter 10 is cheapest for both lanes.
        assertEquals(2, result.selectedTransporters().size());

        // Cheapest feasible solution using two distinct transporters.
        assertEquals(
                0,
                new BigDecimal("40").compareTo(result.totalCost())
        );
    }

    @Test
    void shouldAllowOneTransporterToHandleMultipleLanes() {
        OptimizationInput input = new OptimizationInput(
                List.of(
                        new SolverLane(1L),
                        new SolverLane(2L),
                        new SolverLane(3L)
                ),
                List.of(
                        new SolverTransporter(10L)
                ),
                List.of(
                        new SolverQuote(1L, 10L, new BigDecimal("100")),
                        new SolverQuote(2L, 10L, new BigDecimal("200")),
                        new SolverQuote(3L, 10L, new BigDecimal("300"))
                ),
                2
        );

        OptimizationResult result = solver.solve(input);

        assertEquals("OPTIMAL", result.solverStatus());
        assertEquals(3, result.assignments().size());
        assertEquals(1, result.selectedTransporters().size());

        assertEquals(
                0,
                new BigDecimal("600").compareTo(result.totalCost())
        );
    }

    @Test
    void shouldRejectInfeasibleAssignmentWhenLaneHasNoQuote() {
        OptimizationInput input = new OptimizationInput(
                List.of(
                        new SolverLane(1L),
                        new SolverLane(2L)
                ),
                List.of(
                        new SolverTransporter(10L)
                ),
                List.of(
                        new SolverQuote(1L, 10L, new BigDecimal("100"))
                ),
                1
        );

        assertThrows(
                OptimizationException.class,
                () -> solver.solve(input)
        );
    }

    @Test
    void shouldRejectDuplicateLaneIds() {
        OptimizationInput input = new OptimizationInput(
                List.of(
                        new SolverLane(1L),
                        new SolverLane(1L)
                ),
                List.of(
                        new SolverTransporter(10L)
                ),
                List.of(
                        new SolverQuote(1L, 10L, new BigDecimal("100"))
                ),
                1
        );

        assertThrows(
                OptimizationException.class,
                () -> solver.solve(input)
        );
    }

    @Test
    void shouldRejectDuplicateTransporterIds() {
        OptimizationInput input = new OptimizationInput(
                List.of(
                        new SolverLane(1L)
                ),
                List.of(
                        new SolverTransporter(10L),
                        new SolverTransporter(10L)
                ),
                List.of(
                        new SolverQuote(1L, 10L, new BigDecimal("100"))
                ),
                1
        );

        assertThrows(
                OptimizationException.class,
                () -> solver.solve(input)
        );
    }

    @Test
    void shouldRejectNegativeQuoteAmount() {
        OptimizationInput input = new OptimizationInput(
                List.of(
                        new SolverLane(1L)
                ),
                List.of(
                        new SolverTransporter(10L)
                ),
                List.of(
                        new SolverQuote(1L, 10L, new BigDecimal("-100"))
                ),
                1
        );

        assertThrows(
                OptimizationException.class,
                () -> solver.solve(input)
        );
    }

    @Test
    void shouldAssignEveryLaneExactlyOnce() {
        OptimizationInput input = new OptimizationInput(
                List.of(
                        new SolverLane(1L),
                        new SolverLane(2L),
                        new SolverLane(3L)
                ),
                List.of(
                        new SolverTransporter(10L),
                        new SolverTransporter(20L)
                ),
                List.of(
                        new SolverQuote(1L, 10L, new BigDecimal("100")),
                        new SolverQuote(1L, 20L, new BigDecimal("150")),
                        new SolverQuote(2L, 10L, new BigDecimal("200")),
                        new SolverQuote(2L, 20L, new BigDecimal("250")),
                        new SolverQuote(3L, 10L, new BigDecimal("300")),
                        new SolverQuote(3L, 20L, new BigDecimal("350"))
                ),
                2
        );

        OptimizationResult result = solver.solve(input);

        Set<Long> assignedLaneIds = result.assignments()
                .stream()
                .map(SolverAssignment::laneId)
                .collect(Collectors.toSet());

        assertEquals(Set.of(1L, 2L, 3L), assignedLaneIds);
        assertEquals(3, result.assignments().size());
    }
}
