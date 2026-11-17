
package com.freightfox.assignment.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Configuration for transporter assignment optimization.")
public record AssignmentRequest(

        @Schema(
                description = """
                        Maximum number of distinct transporters allowed
                        in the assignment solution. Must be at least 1.
                        """,
                example = "3",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Min(value = 1, message = "Maximum transporters must be at least 1")
        Integer maxTransporters

) {
}
