package com.freightfox.assignment.assignment.solver;

import java.util.List;

public record OptimizationInput(
        List<SolverLane> lanes,
        List<SolverTransporter> transporters,
        List<SolverQuote> quotes,
        int maxTransporters
) {
}