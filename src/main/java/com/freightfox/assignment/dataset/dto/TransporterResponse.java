package com.freightfox.assignment.dataset.dto;

import java.util.List;

public record TransporterResponse(
        Long id,
        String name,
        List<QuoteResponse> laneQuotes
) {
}