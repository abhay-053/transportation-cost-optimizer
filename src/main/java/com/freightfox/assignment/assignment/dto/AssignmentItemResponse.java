
package com.freightfox.assignment.assignment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Transporter assigned to a particular lane.")
public record AssignmentItemResponse(

        @Schema(
                description = "External ID of the assigned lane.",
                example = "1"
        )
        Long laneId,

        @Schema(
                description = "External ID of the transporter assigned to the lane.",
                example = "101"
        )
        Long transporterId

) {
}
