
package com.freightfox.assignment.dataset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Transportation lane information.")
public record LaneInputRequest(

        @Schema(
                description = "Unique external ID identifying the lane.",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Positive
        Long id,

        @Schema(
                description = "Origin location of the lane.",
                example = "Mumbai",
                maxLength = 150,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(max = 150)
        String origin,

        @Schema(
                description = "Destination location of the lane.",
                example = "Delhi",
                maxLength = 150,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(max = 150)
        String destination

) {
}
