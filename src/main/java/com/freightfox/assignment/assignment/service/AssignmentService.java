
package com.freightfox.assignment.assignment.service;

import com.freightfox.assignment.admin.entity.AdminUser;
import com.freightfox.assignment.admin.repository.AdminUserRepository;

import com.freightfox.assignment.assignment.dto.AssignmentHistoryResponse;
import com.freightfox.assignment.assignment.dto.AssignmentItemResponse;
import com.freightfox.assignment.assignment.dto.AssignmentRequest;
import com.freightfox.assignment.assignment.dto.AssignmentResponse;
import com.freightfox.assignment.assignment.dto.AssignmentRunSummaryResponse;

import com.freightfox.assignment.assignment.entity.AssignmentResult;
import com.freightfox.assignment.assignment.entity.AssignmentRun;

import com.freightfox.assignment.assignment.repository.AssignmentResultRepository;
import com.freightfox.assignment.assignment.repository.AssignmentRunRepository;

import com.freightfox.assignment.assignment.solver.OptimizationInput;
import com.freightfox.assignment.assignment.solver.OptimizationResult;
import com.freightfox.assignment.assignment.solver.SolutionVerifier;
import com.freightfox.assignment.assignment.solver.SolverLane;
import com.freightfox.assignment.assignment.solver.SolverQuote;
import com.freightfox.assignment.assignment.solver.SolverTransporter;
import com.freightfox.assignment.assignment.solver.TransporterOptimizationSolver;

import com.freightfox.assignment.dataset.entity.InputDataset;
import com.freightfox.assignment.dataset.entity.Lane;
import com.freightfox.assignment.dataset.entity.LaneQuote;
import com.freightfox.assignment.dataset.entity.Transporter;

import com.freightfox.assignment.dataset.repository.InputDatasetRepository;
import com.freightfox.assignment.dataset.repository.LaneQuoteRepository;
import com.freightfox.assignment.dataset.repository.LaneRepository;
import com.freightfox.assignment.dataset.repository.TransporterRepository;

import com.freightfox.assignment.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AssignmentService {

    private final AdminUserRepository adminUserRepository;
    private final InputDatasetRepository inputDatasetRepository;
    private final LaneRepository laneRepository;
    private final TransporterRepository transporterRepository;
    private final LaneQuoteRepository laneQuoteRepository;
    private final AssignmentRunRepository assignmentRunRepository;
    private final AssignmentResultRepository assignmentResultRepository;
    private final TransporterOptimizationSolver optimizationSolver;
    private final SolutionVerifier solutionVerifier;

    public AssignmentService(
            AdminUserRepository adminUserRepository,
            InputDatasetRepository inputDatasetRepository,
            LaneRepository laneRepository,
            TransporterRepository transporterRepository,
            LaneQuoteRepository laneQuoteRepository,
            AssignmentRunRepository assignmentRunRepository,
            AssignmentResultRepository assignmentResultRepository,
            TransporterOptimizationSolver optimizationSolver,
            SolutionVerifier solutionVerifier
    ) {
        this.adminUserRepository = adminUserRepository;
        this.inputDatasetRepository = inputDatasetRepository;
        this.laneRepository = laneRepository;
        this.transporterRepository = transporterRepository;
        this.laneQuoteRepository = laneQuoteRepository;
        this.assignmentRunRepository = assignmentRunRepository;
        this.assignmentResultRepository = assignmentResultRepository;
        this.optimizationSolver = optimizationSolver;
        this.solutionVerifier = solutionVerifier;
    }

    /**
     * Optimizes transporter assignments for the latest dataset
     * uploaded by the authenticated admin.
     */
    @Transactional
    public AssignmentResponse optimizeAssignment(
            AssignmentRequest request,
            String adminEmail
    ) {

        AdminUser admin = getAdmin(adminEmail);

        InputDataset dataset = inputDatasetRepository
                .findFirstByAdminIdOrderByCreatedAtDesc(admin.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No input dataset found. Upload lane and transporter data first."
                ));

        // Load dataset records.
        List<Lane> lanes =
                laneRepository.findAllByDatasetId(dataset.getId());

        List<Transporter> transporters =
                transporterRepository.findAllByDatasetId(dataset.getId());

        List<LaneQuote> laneQuotes =
                laneQuoteRepository.findAllByLane_Dataset_Id(dataset.getId());

        if (lanes.isEmpty()) {
            throw new ResourceNotFoundException(
                    "The latest dataset does not contain any lanes."
            );
        }

        if (transporters.isEmpty()) {
            throw new ResourceNotFoundException(
                    "The latest dataset does not contain any transporters."
            );
        }

        // Map external lane IDs to database entities.
        Map<Long, Lane> laneMap = new HashMap<>();
        List<SolverLane> solverLanes = new ArrayList<>();

        for (Lane lane : lanes) {

            Long externalLaneId =
                    Long.parseLong(lane.getLaneCode());

            laneMap.put(externalLaneId, lane);
            solverLanes.add(new SolverLane(externalLaneId));
        }

        // Map external transporter IDs to database entities.
        Map<Long, Transporter> transporterMap = new HashMap<>();
        List<SolverTransporter> solverTransporters =
                new ArrayList<>();

        for (Transporter transporter : transporters) {

            Long externalTransporterId =
                    Long.parseLong(transporter.getTransporterCode());

            transporterMap.put(externalTransporterId, transporter);

            solverTransporters.add(
                    new SolverTransporter(externalTransporterId)
            );
        }

        // Convert database quotes into solver input.
        List<SolverQuote> solverQuotes = new ArrayList<>();

        for (LaneQuote laneQuote : laneQuotes) {

            Long externalLaneId =
                    Long.parseLong(
                            laneQuote.getLane().getLaneCode()
                    );

            Long externalTransporterId =
                    Long.parseLong(
                            laneQuote.getTransporter().getTransporterCode()
                    );

            solverQuotes.add(
                    new SolverQuote(
                            externalLaneId,
                            externalTransporterId,
                            laneQuote.getQuoteAmount()
                    )
            );
        }

        OptimizationInput optimizationInput =
                new OptimizationInput(
                        solverLanes,
                        solverTransporters,
                        solverQuotes,
                        request.maxTransporters()
                );

        // Run optimization.
        OptimizationResult result =
                optimizationSolver.solve(optimizationInput);

        // Independently verify the solver result.
        // No assignment data is persisted before verification succeeds.
        solutionVerifier.verify(optimizationInput, result);

        // Save the assignment run.
        AssignmentRun assignmentRun =
                assignmentRunRepository.save(
                        new AssignmentRun(
                                admin,
                                dataset,
                                request.maxTransporters(),
                                result.selectedTransporters().size(),
                                result.totalCost(),
                                result.solverStatus()
                        )
                );

        // Build quote lookup for result persistence.
        Map<QuoteKey, BigDecimal> quoteMap = new HashMap<>();

        for (SolverQuote quote : solverQuotes) {

            quoteMap.put(
                    new QuoteKey(
                            quote.laneId(),
                            quote.transporterId()
                    ),
                    quote.amount()
            );
        }

        List<AssignmentResult> assignmentResults =
                new ArrayList<>();

        List<AssignmentItemResponse> assignmentResponses =
                new ArrayList<>();

        // Convert solver assignments into database entities.
        for (var solverAssignment : result.assignments()) {

            Lane lane =
                    laneMap.get(solverAssignment.laneId());

            Transporter transporter =
                    transporterMap.get(
                            solverAssignment.transporterId()
                    );

            if (lane == null || transporter == null) {
                throw new IllegalStateException(
                        "Solver returned an assignment for an unknown lane or transporter."
                );
            }

            BigDecimal quoteAmount = quoteMap.get(
                    new QuoteKey(
                            solverAssignment.laneId(),
                            solverAssignment.transporterId()
                    )
            );

            if (quoteAmount == null) {
                throw new IllegalStateException(
                        "No quote found for a solver-generated assignment."
                );
            }

            assignmentResults.add(
                    new AssignmentResult(
                            assignmentRun,
                            lane,
                            transporter,
                            quoteAmount
                    )
            );

            assignmentResponses.add(
                    new AssignmentItemResponse(
                            solverAssignment.laneId(),
                            solverAssignment.transporterId()
                    )
            );
        }

        assignmentResultRepository.saveAll(assignmentResults);

        // Return the original API response structure.
        return new AssignmentResponse(
                "success",
                result.totalCost(),
                assignmentResponses,
                result.selectedTransporters()
        );
    }

    /**
     * Returns all assignment runs belonging to the logged-in admin.
     */
    @Transactional(readOnly = true)
    public List<AssignmentRunSummaryResponse> getAssignmentHistory(
            String adminEmail
    ) {

        AdminUser admin = getAdmin(adminEmail);

        return assignmentRunRepository
                .findAllByAdminIdOrderByCreatedAtDesc(admin.getId())
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    /**
     * Returns one assignment run with its lane assignments.
     */
    @Transactional(readOnly = true)
    public AssignmentHistoryResponse getAssignmentHistoryById(
            Long assignmentId,
            String adminEmail
    ) {

        AdminUser admin = getAdmin(adminEmail);

        AssignmentRun assignmentRun = assignmentRunRepository
                .findByIdAndAdminId(
                        assignmentId,
                        admin.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assignment history not found."
                ));

        List<AssignmentResult> results =
                assignmentResultRepository.findAllByAssignmentRunId(
                        assignmentRun.getId()
                );

        List<AssignmentItemResponse> assignments =
                results.stream()
                        .map(result -> new AssignmentItemResponse(
                                Long.parseLong(
                                        result.getLane().getLaneCode()
                                ),
                                Long.parseLong(
                                        result.getTransporter()
                                                .getTransporterCode()
                                )
                        ))
                        .sorted(
                                Comparator.comparing(
                                        AssignmentItemResponse::laneId
                                )
                        )
                        .toList();

        List<Long> selectedTransporters =
                assignments.stream()
                        .map(AssignmentItemResponse::transporterId)
                        .distinct()
                        .sorted()
                        .toList();

        return new AssignmentHistoryResponse(
                assignmentRun.getId(),
                assignmentRun.getDataset().getId(),
                assignmentRun.getMaxTransporters(),
                assignmentRun.getSelectedTransporterCount(),
                assignmentRun.getTotalCost(),
                assignmentRun.getSolverStatus(),
                assignmentRun.getCreatedAt(),
                assignments,
                selectedTransporters
        );
    }

    /**
     * Returns assignment runs associated with a dataset
     * owned by the authenticated admin.
     */
    @Transactional(readOnly = true)
    public List<AssignmentRunSummaryResponse> getDatasetAssignmentHistory(
            Long datasetId,
            String adminEmail
    ) {

        AdminUser admin = getAdmin(adminEmail);

        inputDatasetRepository
                .findByIdAndAdminId(datasetId, admin.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dataset not found."
                ));

        return assignmentRunRepository
                .findAllByDatasetIdAndAdminIdOrderByCreatedAtDesc(
                        datasetId,
                        admin.getId()
                )
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    private AdminUser getAdmin(String email) {

        return adminUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Admin account not found."
                ));
    }

    private AssignmentRunSummaryResponse toSummaryResponse(
            AssignmentRun assignmentRun
    ) {

        return new AssignmentRunSummaryResponse(
                assignmentRun.getId(),
                assignmentRun.getDataset().getId(),
                assignmentRun.getMaxTransporters(),
                assignmentRun.getSelectedTransporterCount(),
                assignmentRun.getTotalCost(),
                assignmentRun.getSolverStatus(),
                assignmentRun.getCreatedAt()
        );
    }

    private record QuoteKey(
            Long laneId,
            Long transporterId
    ) {
    }
}
