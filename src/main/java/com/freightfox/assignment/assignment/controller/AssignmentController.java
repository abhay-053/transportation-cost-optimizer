
package com.freightfox.assignment.assignment.controller;

import com.freightfox.assignment.assignment.dto.AssignmentHistoryResponse;
import com.freightfox.assignment.assignment.dto.AssignmentRequest;
import com.freightfox.assignment.assignment.dto.AssignmentResponse;
import com.freightfox.assignment.assignment.dto.AssignmentRunSummaryResponse;
import com.freightfox.assignment.assignment.service.AssignmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(
        name = "Transporter Assignment",
        description = "Transporter optimization and assignment history APIs"
)
@SecurityRequirement(name = "Bearer Authentication")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @Operation(
            summary = "Optimize transporter assignments",
            description = """
                    Calculates an optimal assignment of transporters to all lanes
                    in the latest dataset uploaded by the authenticated admin.

                    The optimization follows two objectives:
                    1. Maximize the number of distinct transporters used,
                       without exceeding maxTransporters.
                    2. Among solutions with the maximum number of distinct
                       transporters, minimize the total transportation cost.

                    Every lane must receive exactly one eligible transporter.
                    A transporter without a quote for a lane is not eligible
                    for that lane.

                    A successful response includes the assignments, total cost,
                    and selected transporters. The assignment run is also saved
                    in assignment history.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Optimal assignment generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid assignment request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "No dataset found for the authenticated admin"),
            @ApiResponse(responseCode = "422", description = "No feasible assignment exists or an optimal solution could not be established")
    })
    @PostMapping("/transporters/assignment")
    public ResponseEntity<AssignmentResponse> optimizeAssignment(
            @Valid @RequestBody AssignmentRequest request,
            Authentication authentication) {

        AssignmentResponse response =
                assignmentService.optimizeAssignment(
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get assignment history",
            description = """
                    Returns a list of all assignment runs created by
                    the authenticated admin, ordered by creation time.
                    Each entry provides summary information about an assignment run.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assignment history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentRunSummaryResponse>>
    getAssignmentHistory(Authentication authentication) {

        List<AssignmentRunSummaryResponse> response =
                assignmentService.getAssignmentHistory(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get assignment run details",
            description = """
                    Returns the detailed results of a specific assignment run,
                    including its assignments and optimization information.

                    Only assignment runs belonging to the authenticated admin
                    can be accessed.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assignment details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Assignment run not found")
    })
    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<AssignmentHistoryResponse> getAssignmentHistoryById(
            @Parameter(description = "ID of the assignment run", example = "1")
            @PathVariable Long assignmentId,
            Authentication authentication) {

        AssignmentHistoryResponse response =
                assignmentService.getAssignmentHistoryById(
                        assignmentId,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get assignment history for a dataset",
            description = """
                    Returns all assignment runs associated with the specified dataset.

                    Only datasets and assignment runs belonging to
                    the authenticated admin are accessible.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dataset assignment history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Dataset not found")
    })
    @GetMapping("/datasets/{datasetId}/assignments")
    public ResponseEntity<List<AssignmentRunSummaryResponse>>
    getDatasetAssignmentHistory(
            @Parameter(description = "ID of the dataset", example = "1")
            @PathVariable Long datasetId,
            Authentication authentication) {

        List<AssignmentRunSummaryResponse> response =
                assignmentService.getDatasetAssignmentHistory(
                        datasetId,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }
}
