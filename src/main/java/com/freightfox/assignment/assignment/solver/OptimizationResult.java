package com.freightfox.assignment.assignment.solver;

import java.math.BigDecimal;
import java.util.List;

public record OptimizationResult(
        String solverStatus,
        BigDecimal totalCost,
        List<SolverAssignment> assignments,
        List<Long> selectedTransporters
) {
}