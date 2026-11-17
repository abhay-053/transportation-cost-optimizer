
package com.freightfox.assignment.assignment.solver;

import com.freightfox.assignment.exception.OptimizationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolutionVerifierTest {

    private SolutionVerifier solutionVerifier;

    private OptimizationInput validInput;

    @BeforeEach
    void setUp() {
        solutionVerifier = new SolutionVerifier();

        validInput = new OptimizationInput(
                List.of(
                        new SolverLane(1L),
                        new SolverLane(2L)
                ),
                List.of(
                        new SolverTransporter(10L),
                        new SolverTransporter(20L)
                ),
                List.of(
                        new SolverQuote(1L, 10L, new BigDecimal("100.00")),
                        new SolverQuote(1L, 20L, new BigDecimal("150.00")),
                        new SolverQuote(2L, 10L, new BigDecimal("200.00")),
                        new SolverQuote(2L, 20L, new BigDecimal("250.00"))
                ),
                2
        );
    }

    private OptimizationResult validResult() {
        return new OptimizationResult(
                "OPTIMAL",
                new BigDecimal("350.00"),
                List.of(
                        new SolverAssignment(1L, 10L),
                        new SolverAssignment(2L, 20L)
                ),
                List.of(10L, 20L)
        );
    }

    @Test
    void shouldAcceptValidOptimizationResult() {
        assertDoesNotThrow(
                () -> solutionVerifier.verify(validInput, validResult())
        );
    }

    @Test
    void shouldRejectNullInput() {
        assertThrows(
                OptimizationException.class,
                () -> solutionVerifier.verify(null, validResult())
        );
    }

    @Test
    void shouldRejectNullResult() {
        assertThrows(
                OptimizationException.class,
                () -> solutionVerifier.verify(validInput, null)
        );
    }

    @Test
    void shouldRejectMissingLaneAssignment() {
        OptimizationResult result = new OptimizationResult(
                "OPTIMAL",
                new BigDecimal("100.00"),
                List.of(
                        new SolverAssignment(1L, 10L)
                ),
                List.of(10L)
        );

        assertThrows(
                OptimizationException.class,
                () -> solutionVerifier.verify(validInput, result)
        );
    }

    @Test
    void shouldRejectDuplicateLaneAssignment() {
        OptimizationResult result = new OptimizationResult(
                "OPTIMAL",
                new BigDecimal("200.00"),
                List.of(
                        new SolverAssignment(1L, 10L),
                        new SolverAssignment(1L, 20L),
                        new SolverAssignment(2L, 10L)
                ),
                List.of(10L, 20L)
        );

        assertThrows(
                OptimizationException.class,
                () -> solutionVerifier.verify(validInput, result)
        );
    }

    @Test
    void shouldRejectUnknownLane() {
        OptimizationResult result = new OptimizationResult(
                "OPTIMAL",
                new BigDecimal("350.00"),
                List.of(
                        new SolverAssignment(99L, 10L),
                        new SolverAssignment(2L, 20L)
                ),
                List.of(10L, 20L)
        );

        assertThrows(
                OptimizationException.class,
                () -> solutionVerifier.verify(validInput, result)
        );
    }

    @Test
    void shouldRejectUnknownTransporter() {
        OptimizationResult result = new OptimizationResult(
                "OPTIMAL",
                new BigDecimal("350.00"),
                List.of(
                        new SolverAssignment(1L, 99L),
                        new SolverAssignment(2L, 20L)
                ),
                List.of(99L, 20L)
        );

        assertThrows(
                OptimizationException.class,
                () -> solutionVerifier.verify(validInput, result)
        );
    }

    @Test
    void shouldRejectAssignmentWithoutQuote() {
        OptimizationResult result = new OptimizationResult(
                "OPTIMAL",
                new BigDecimal("350.00"),
                List.of(
                        new SolverAssignment(1L, 10L),
                        new SolverAssignment(2L, 99L)
                ),
                List.of(10L, 99L)
        );

        assertThrows(
                OptimizationException.class,
                () -> solutionVerifier.verify(validInput, result)
        );
    }

    @Test
    void shouldRejectExceededTransporterLimit() {
        OptimizationInput input = new OptimizationInput(
                validInput.lanes(),
                validInput.transporters(),
                validInput.quotes(),
                1
        );

        OptimizationResult result = validResult();

        assertThrows(
                OptimizationException.class,
                () -> solutionVerifier.verify(input, result)
        );
    }

    @Test
    void shouldRejectMismatchBetweenSelectedAndUsedTransporters() {
        OptimizationResult result = new OptimizationResult(
                "OPTIMAL",
                new BigDecimal("350.00"),
                List.of(
                        new SolverAssignment(1L, 10L),
                        new SolverAssignment(2L, 20L)
                ),
                List.of(10L)
        );

        assertThrows(
                OptimizationException.class,
                () -> solutionVerifier.verify(validInput, result)
        );
    }

    @Test
    void shouldRejectIncorrectTotalCost() {
        OptimizationResult result = new OptimizationResult(
                "OPTIMAL",
                new BigDecimal("300.00"),
                List.of(
                        new SolverAssignment(1L, 10L),
                        new SolverAssignment(2L, 20L)
                ),
                List.of(10L, 20L)
        );

        assertThrows(
                OptimizationException.class,
                () -> solutionVerifier.verify(validInput, result)
        );
    }

    @Test
    void shouldRejectNonOptimalSolverStatus() {
        OptimizationResult result = new OptimizationResult(
                "FEASIBLE",
                new BigDecimal("350.00"),
                List.of(
                        new SolverAssignment(1L, 10L),
                        new SolverAssignment(2L, 20L)
                ),
                List.of(10L, 20L)
        );

        assertThrows(
                OptimizationException.class,
                () -> solutionVerifier.verify(validInput, result)
        );
    }

    @Test
    void shouldRejectDuplicateSelectedTransporters() {
        OptimizationResult result = new OptimizationResult(
                "OPTIMAL",
                new BigDecimal("350.00"),
                List.of(
                        new SolverAssignment(1L, 10L),
                        new SolverAssignment(2L, 20L)
                ),
                List.of(10L, 10L, 20L)
        );

        assertThrows(
                OptimizationException.class,
                () -> solutionVerifier.verify(validInput, result)
        );
    }
}
