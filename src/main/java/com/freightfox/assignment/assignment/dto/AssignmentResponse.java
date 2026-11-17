
package com.freightfox.assignment.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Result of transporter assignment optimization.")
public record AssignmentResponse(

        @Schema(
                description = "Optimization result status.",
                example = "OPTIMAL"
        )
        String status,

        @Schema(
                description = "Minimum total transportation cost for the selected optimal solution.",
                example = "14000.00"
        )
        BigDecimal totalCost,

        @Schema(
                description = "Lane-wise transporter allocation.",
                example = """
                        [
                          {
                            "laneId": 1,
                            "transporterId": 101
                          },
                          {
                            "laneId": 2,
                            "transporterId": 103
                          }
                        ]
                        """
        )
        List<AssignmentItemResponse> assignments,

        @Schema(
                description = "External IDs of all distinct transporters selected in the solution.",
                example = "[101, 103]"
        )
        List<Long> selectedTransporters

) {
}
