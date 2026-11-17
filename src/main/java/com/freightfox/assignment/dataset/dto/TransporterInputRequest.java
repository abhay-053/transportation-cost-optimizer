
package com.freightfox.assignment.dataset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Transporter details and its available lane quotes.")
public record TransporterInputRequest(

        @Schema(
                description = "Unique external ID identifying the transporter.",
                example = "101",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Positive
        Long id,

        @Schema(
                description = "Name of the transporter.",
                example = "Blue Dart Logistics",
                maxLength = 150,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(max = 150)
        String name,

        @Schema(
                description = "Quotes submitted by this transporter for individual lanes.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty
        List<@Valid QuoteInputRequest> laneQuotes

) {
}
