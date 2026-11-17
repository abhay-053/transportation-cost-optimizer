package com.freightfox.assignment.assignment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssignmentRunSummaryResponse(
        Long assignmentId,
        Long datasetId,
        Integer maxTransporters,
        Integer selectedTransporterCount,
        BigDecimal totalCost,
        String solverStatus,
        LocalDateTime createdAt
) {
}