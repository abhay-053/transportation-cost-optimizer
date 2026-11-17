package com.freightfox.assignment.dataset.dto;

import java.math.BigDecimal;

public record QuoteResponse(
        Long laneId,
        BigDecimal quote
) {
}