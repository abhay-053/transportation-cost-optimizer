
package com.freightfox.assignment.assignment.solver;

import com.freightfox.assignment.exception.OptimizationException;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class SolutionVerifier {

    /**
     * Independently verifies the structural correctness and total cost
     * of the optimization result before it is persisted.
     */
    public void verify(
            OptimizationInput input,
            OptimizationResult result
    ) {
        if (input == null || result == null) {
            throw new OptimizationException(
                    "Optimization input or result cannot be null."
            );
        }

        if (result.assignments() == null
                || result.selectedTransporters() == null
                || result.totalCost() == null) {
            throw new OptimizationException(
                    "Solver returned an incomplete optimization result."
            );
        }

        Set<Long> laneIds = new HashSet<>();
        input.lanes().forEach(lane -> laneIds.add(lane.id()));

        Set<Long> transporterIds = new HashSet<>();
        input.transporters().forEach(
                transporter -> transporterIds.add(transporter.id())
        );

        // Build a lookup of all valid lane-transporter quotes.
        Map<QuoteKey, BigDecimal> quoteMap = new HashMap<>();

        for (SolverQuote quote : input.quotes()) {
            quoteMap.put(
                    new QuoteKey(
                            quote.laneId(),
                            quote.transporterId()
                    ),
                    quote.amount()
            );
        }

        Set<Long> assignedLanes = new HashSet<>();
        Set<Long> actuallyUsedTransporters = new HashSet<>();

        BigDecimal recalculatedTotalCost = BigDecimal.ZERO;

        for (SolverAssignment assignment : result.assignments()) {

            if (!laneIds.contains(assignment.laneId())) {
                throw new OptimizationException(
                        "Solver assigned an unknown lane: "
                                + assignment.laneId()
                );
            }

            if (!transporterIds.contains(assignment.transporterId())) {
                throw new OptimizationException(
                        "Solver used an unknown transporter: "
                                + assignment.transporterId()
                );
            }

            if (!assignedLanes.add(assignment.laneId())) {
                throw new OptimizationException(
                        "A lane has been assigned more than once: "
                                + assignment.laneId()
                );
            }

            QuoteKey key = new QuoteKey(
                    assignment.laneId(),
                    assignment.transporterId()
            );

            BigDecimal quoteAmount = quoteMap.get(key);

            if (quoteAmount == null) {
                throw new OptimizationException(
                        "Assignment has no corresponding transporter quote. "
                                + "Lane: " + assignment.laneId()
                                + ", transporter: "
                                + assignment.transporterId()
                );
            }

            actuallyUsedTransporters.add(
                    assignment.transporterId()
            );

            recalculatedTotalCost =
                    recalculatedTotalCost.add(quoteAmount);
        }

        // Every lane must be assigned exactly once.
        if (!assignedLanes.equals(laneIds)) {
            Set<Long> unassignedLanes = new HashSet<>(laneIds);
            unassignedLanes.removeAll(assignedLanes);

            throw new OptimizationException(
                    "Some lanes have not been assigned: "
                            + unassignedLanes
            );
        }

        // Check that the selected transporter list contains no duplicates.
        Set<Long> selectedTransporters =
                new HashSet<>(result.selectedTransporters());

        if (selectedTransporters.size()
                != result.selectedTransporters().size()) {
            throw new OptimizationException(
                    "Selected transporter list contains duplicates."
            );
        }

        // Every selected transporter must exist in the input.
        if (!transporterIds.containsAll(selectedTransporters)) {
            throw new OptimizationException(
                    "Solver selected an unknown transporter."
            );
        }

        // Selected transporters must be exactly those actually used.
        if (!selectedTransporters.equals(actuallyUsedTransporters)) {
            throw new OptimizationException(
                    "Selected transporter list does not match "
                            + "the actual assignments."
            );
        }

        // Enforce the requested transporter limit.
        if (actuallyUsedTransporters.size() > input.maxTransporters()) {
            throw new OptimizationException(
                    "Solver exceeded the maximum transporter limit."
            );
        }

        // Independently recalculate and compare the total cost.
        if (recalculatedTotalCost.compareTo(result.totalCost()) != 0) {
            throw new OptimizationException(
                    "Total cost verification failed. "
                            + "Expected: " + recalculatedTotalCost
                            + ", received: " + result.totalCost()
            );
        }

        // The solver must report a completed optimal result.
        if (!"OPTIMAL".equals(result.solverStatus())) {
            throw new OptimizationException(
                    "Solver did not return an optimal solution. Status: "
                            + result.solverStatus()
            );
        }
    }

    private record QuoteKey(
            Long laneId,
            Long transporterId
    ) {
    }
}
