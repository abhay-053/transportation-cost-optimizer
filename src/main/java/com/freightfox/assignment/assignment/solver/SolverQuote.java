package com.freightfox.assignment.assignment.solver;

import java.math.BigDecimal;

public record SolverQuote(
        Long laneId,
        Long transporterId,
        BigDecimal amount
) {
}