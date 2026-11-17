package com.freightfox.assignment.dataset.dto;

import java.time.LocalDateTime;

public record DatasetResponse(
        Long id,
        String datasetName,
        LocalDateTime createdAt,
        long totalLanes,
        long totalTransporters
) {
}