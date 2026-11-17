package com.freightfox.assignment.assignment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AssignmentHistoryResponse(
        Long assignmentId,
        Long datasetId,
        Integer maxTransporters,
        Integer selectedTransporterCount,
        BigDecimal totalCost,
        String solverStatus,
        LocalDateTime createdAt,
        List<AssignmentItemResponse> assignments,
        List<Long> selectedTransporters
) {
}