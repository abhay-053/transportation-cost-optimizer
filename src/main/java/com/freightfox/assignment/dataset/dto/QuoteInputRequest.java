
package com.freightfox.assignment.dataset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Price quoted by a transporter for a particular lane.")
public record QuoteInputRequest(

        @Schema(
                description = "External ID of the lane for which the quote is submitted.",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Positive
        Long laneId,

        @Schema(
                description = "Transportation cost quoted for the lane. Must be non-negative.",
                example = "5000.00",
                minimum = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @DecimalMin("0.00")
        BigDecimal quote

) {
}
