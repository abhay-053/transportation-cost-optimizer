
package com.freightfox.assignment.dataset.controller;

import com.freightfox.assignment.dataset.dto.*;
import com.freightfox.assignment.dataset.service.DatasetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(
        name = "Dataset Management",
        description = "Upload and retrieve lane and transporter datasets"
)
@SecurityRequirement(name = "Bearer Authentication")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    @Operation(
            summary = "Upload lane and transporter data",
            description = """
                    Uploads a new dataset containing lanes, transporters,
                    and their quoted prices for individual lanes.

                    Each transporter can provide quotes for multiple lanes.
                    The uploaded dataset is associated with the authenticated admin
                    and becomes their latest dataset for transporter optimization.

                    Duplicate IDs, unknown lane references, and duplicate quotes
                    are rejected during validation.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dataset uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping("/transporters/input")
    public ResponseEntity<InputDataResponse> submitInputData(
            @Valid @RequestBody InputDataRequest request,
            Authentication authentication) {

        InputDataResponse response = datasetService.saveInputData(
                request,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all uploaded datasets",
            description = """
                    Returns all datasets uploaded by the authenticated admin,
                    ordered from newest to oldest.

                    Each entry includes dataset metadata and the number
                    of lanes and transporters.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datasets retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/datasets")
    public ResponseEntity<List<DatasetResponse>> getAllDatasets(
            Authentication authentication) {

        return ResponseEntity.ok(
                datasetService.getAllDatasets(authentication.getName())
        );
    }

    @Operation(
            summary = "Get dataset details",
            description = """
                    Returns metadata for a specific dataset.

                    Only datasets belonging to the authenticated admin
                    can be accessed.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dataset retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Dataset not found")
    })
    @GetMapping("/datasets/{datasetId}")
    public ResponseEntity<DatasetResponse> getDataset(
            @Parameter(description = "ID of the dataset", example = "1")
            @PathVariable Long datasetId,
            Authentication authentication) {

        return ResponseEntity.ok(
                datasetService.getDataset(
                        datasetId,
                        authentication.getName()
                )
        );
    }

    @Operation(
            summary = "Get lanes in a dataset",
            description = """
                    Returns all lanes associated with the specified dataset,
                    including their external lane IDs, origins, and destinations.

                    The dataset must belong to the authenticated admin.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lanes retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Dataset not found")
    })
    @GetMapping("/datasets/{datasetId}/lanes")
    public ResponseEntity<List<LaneResponse>> getDatasetLanes(
            @Parameter(description = "ID of the dataset", example = "1")
            @PathVariable Long datasetId,
            Authentication authentication) {

        return ResponseEntity.ok(
                datasetService.getDatasetLanes(
                        datasetId,
                        authentication.getName()
                )
        );
    }

    @Operation(
            summary = "Get transporters and their quotes",
            description = """
                    Returns all transporters in the specified dataset,
                    along with the lane IDs and quoted prices provided by each transporter.

                    The dataset must belong to the authenticated admin.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transporters retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Dataset not found")
    })
    @GetMapping("/datasets/{datasetId}/transporters")
    public ResponseEntity<List<TransporterResponse>> getDatasetTransporters(
            @Parameter(description = "ID of the dataset", example = "1")
            @PathVariable Long datasetId,
            Authentication authentication) {

        return ResponseEntity.ok(
                datasetService.getDatasetTransporters(
                        datasetId,
                        authentication.getName()
                )
        );
    }
}
