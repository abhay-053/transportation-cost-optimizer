
package com.freightfox.assignment.dataset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Input dataset containing transportation lanes and transporter quotes.")
public record InputDataRequest(

        @Schema(
                description = "List of transportation lanes to be included in the dataset.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty
        List<@Valid LaneInputRequest> lanes,

        @Schema(
                description = "List of transporters and their quotes for the specified lanes.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty
        List<@Valid TransporterInputRequest> transporters

) {
}
