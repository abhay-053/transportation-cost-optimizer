package com.freightfox.assignment.assignment.solver;

import com.freightfox.assignment.exception.OptimizationException;

import com.google.ortools.Loader;
import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TransporterOptimizationSolver {

    private static final double MAX_SOLVER_TIME_SECONDS = 60.0;

    static {
        Loader.loadNativeLibraries();
    }

    public OptimizationResult solve(OptimizationInput input) {

        validateInput(input);

        /*
         * Stage 1:
         * Maximize the number of distinct transporters used.
         */
        CpModel firstModel = new CpModel();

        ModelVariables firstVariables =
                buildModel(firstModel, input);

        LinearExprBuilder transporterObjective =
                LinearExpr.newBuilder();

        for (BoolVar transporterVariable :
                firstVariables.transporterVariables().values()) {

            transporterObjective.addTerm(transporterVariable, 1);
        }

        firstModel.maximize(transporterObjective);

        CpSolver firstSolver = new CpSolver();
        firstSolver.getParameters()
                .setMaxTimeInSeconds(MAX_SOLVER_TIME_SECONDS);

        CpSolverStatus firstStatus = firstSolver.solve(firstModel);

        if (firstStatus == CpSolverStatus.INFEASIBLE) {
            throw new OptimizationException(
                    "No feasible assignment exists. Every lane must have an eligible transporter."
            );
        }

        if (firstStatus != CpSolverStatus.OPTIMAL) {
            throw new OptimizationException(
                    "Could not prove the maximum number of transporters within the solver time limit. Status: "
                            + firstStatus
            );
        }

        int maximumTransporterCount = 0;

        for (BoolVar variable :
                firstVariables.transporterVariables().values()) {

            if (firstSolver.booleanValue(variable)) {
                maximumTransporterCount++;
            }
        }

        /*
         * Stage 2:
         * Fix the maximum transporter count found in Stage 1.
         * Then minimize total quotation cost.
         */
        CpModel secondModel = new CpModel();

        ModelVariables secondVariables =
                buildModel(secondModel, input);

        LinearExprBuilder selectedTransporterCount =
                LinearExpr.newBuilder();

        for (BoolVar variable :
                secondVariables.transporterVariables().values()) {

            selectedTransporterCount.addTerm(variable, 1);
        }

        secondModel.addEquality(
                selectedTransporterCount,
                maximumTransporterCount
        );

        LinearExprBuilder costObjective =
                LinearExpr.newBuilder();

        for (Map.Entry<QuoteKey, BoolVar> entry :
                secondVariables.assignmentVariables().entrySet()) {

            long quoteInMinorUnits =
                    toMinorUnits(
                            secondVariables.quoteAmounts().get(entry.getKey())
                    );

            costObjective.addTerm(
                    entry.getValue(),
                    quoteInMinorUnits
            );
        }

        secondModel.minimize(costObjective);

        CpSolver secondSolver = new CpSolver();

        secondSolver.getParameters()
                .setMaxTimeInSeconds(MAX_SOLVER_TIME_SECONDS);

        CpSolverStatus secondStatus =
                secondSolver.solve(secondModel);

        if (secondStatus != CpSolverStatus.OPTIMAL) {
            throw new OptimizationException(
                    "Could not prove the minimum assignment cost within the solver time limit. Status: "
                            + secondStatus
            );
        }

        return buildResult(
                secondSolver,
                secondVariables,
                input
        );
    }

    private ModelVariables buildModel(
            CpModel model,
            OptimizationInput input) {

        Map<QuoteKey, BoolVar> assignmentVariables =
                new HashMap<>();

        Map<Long, BoolVar> transporterVariables =
                new HashMap<>();

        Map<QuoteKey, BigDecimal> quoteAmounts =
                new HashMap<>();

        Set<Long> laneIds = new HashSet<>();

        for (SolverLane lane : input.lanes()) {
            laneIds.add(lane.id());
        }

        for (SolverTransporter transporter : input.transporters()) {

            transporterVariables.put(
                    transporter.id(),
                    model.newBoolVar(
                            "transporter_" + transporter.id()
                    )
            );
        }

        for (SolverQuote quote : input.quotes()) {

            QuoteKey key = new QuoteKey(
                    quote.laneId(),
                    quote.transporterId()
            );

            BoolVar assignmentVariable =
                    model.newBoolVar(
                            "assignment_" +
                                    quote.laneId() + "_" +
                                    quote.transporterId()
                    );

            assignmentVariables.put(key, assignmentVariable);
            quoteAmounts.put(key, quote.amount());

            BoolVar transporterVariable =
                    transporterVariables.get(quote.transporterId());

            // x_ij <= y_j
            model.addLessOrEqual(
                    assignmentVariable,
                    transporterVariable
            );
        }

        /*
         * Every lane must be assigned to exactly one transporter.
         */
        for (SolverLane lane : input.lanes()) {

            LinearExprBuilder laneAssignments =
                    LinearExpr.newBuilder();

            for (Map.Entry<QuoteKey, BoolVar> entry :
                    assignmentVariables.entrySet()) {

                if (entry.getKey().laneId().equals(lane.id())) {
                    laneAssignments.addTerm(entry.getValue(), 1);
                }
            }

            model.addEquality(laneAssignments, 1);
        }

        /*
         * A transporter is selected if and only if
         * it receives at least one lane.
         */
        for (SolverTransporter transporter : input.transporters()) {

            BoolVar transporterVariable =
                    transporterVariables.get(transporter.id());

            LinearExprBuilder transporterAssignments =
                    LinearExpr.newBuilder();

            for (Map.Entry<QuoteKey, BoolVar> entry :
                    assignmentVariables.entrySet()) {

                if (entry.getKey().transporterId()
                        .equals(transporter.id())) {

                    transporterAssignments.addTerm(
                            entry.getValue(),
                            1
                    );
                }
            }

            // y_j <= sum_i(x_ij)
            model.addLessOrEqual(
                    transporterVariable,
                    transporterAssignments
            );
        }

        /*
         * Maximum allowed number of selected transporters.
         */
        LinearExprBuilder transporterLimit =
                LinearExpr.newBuilder();

        for (BoolVar variable : transporterVariables.values()) {
            transporterLimit.addTerm(variable, 1);
        }

        model.addLessOrEqual(
                transporterLimit,
                input.maxTransporters()
        );

        return new ModelVariables(
                assignmentVariables,
                transporterVariables,
                quoteAmounts
        );
    }

    private OptimizationResult buildResult(
            CpSolver solver,
            ModelVariables variables,
            OptimizationInput input) {

        List<SolverAssignment> assignments =
                new ArrayList<>();

        List<Long> selectedTransporters =
                new ArrayList<>();

        long totalCostMinorUnits = 0;

        for (Map.Entry<QuoteKey, BoolVar> entry :
                variables.assignmentVariables().entrySet()) {

            if (!solver.booleanValue(entry.getValue())) {
                continue;
            }

            QuoteKey key = entry.getKey();

            assignments.add(
                    new SolverAssignment(
                            key.laneId(),
                            key.transporterId()
                    )
            );

            totalCostMinorUnits += toMinorUnits(
                    variables.quoteAmounts().get(key)
            );
        }

        for (Map.Entry<Long, BoolVar> entry :
                variables.transporterVariables().entrySet()) {

            if (solver.booleanValue(entry.getValue())) {
                selectedTransporters.add(entry.getKey());
            }
        }

        assignments.sort(
                (a, b) -> a.laneId().compareTo(b.laneId())
        );

        selectedTransporters.sort(Long::compareTo);

        BigDecimal totalCost = BigDecimal.valueOf(
                totalCostMinorUnits, 2
        );

        return new OptimizationResult(
                "OPTIMAL",
                totalCost,
                assignments,
                selectedTransporters
        );
    }

    private long toMinorUnits(BigDecimal amount) {

        try {
            return amount
                    .setScale(2, RoundingMode.UNNECESSARY)
                    .movePointRight(2)
                    .longValueExact();

        } catch (ArithmeticException exception) {
            throw new OptimizationException(
                    "Quote amount exceeds supported precision or range"
            );
        }
    }

    private void validateInput(OptimizationInput input) {

        if (input == null
                || input.lanes() == null
                || input.transporters() == null
                || input.quotes() == null
                || input.lanes().isEmpty()
                || input.transporters().isEmpty()
                || input.maxTransporters() < 1) {

            throw new OptimizationException(
                    "Invalid optimization input"
            );
        }

        Set<Long> laneIds = new HashSet<>();

        for (SolverLane lane : input.lanes()) {
            if (lane.id() == null || !laneIds.add(lane.id())) {
                throw new OptimizationException(
                        "Lane IDs must be non-null and unique"
                );
            }
        }

        Set<Long> transporterIds = new HashSet<>();

        for (SolverTransporter transporter : input.transporters()) {
            if (transporter.id() == null
                    || !transporterIds.add(transporter.id())) {

                throw new OptimizationException(
                        "Transporter IDs must be non-null and unique"
                );
            }
        }

        Set<QuoteKey> quoteKeys = new HashSet<>();

        for (SolverQuote quote : input.quotes()) {

            if (quote.laneId() == null
                    || quote.transporterId() == null
                    || quote.amount() == null
                    || quote.amount().signum() < 0) {

                throw new OptimizationException(
                        "Invalid transporter quote"
                );
            }

            if (!laneIds.contains(quote.laneId())
                    || !transporterIds.contains(quote.transporterId())) {

                throw new OptimizationException(
                        "Quote references an unknown lane or transporter"
                );
            }

            QuoteKey key = new QuoteKey(
                    quote.laneId(),
                    quote.transporterId()
            );

            if (!quoteKeys.add(key)) {
                throw new OptimizationException(
                        "Duplicate quote for lane " + quote.laneId()
                                + " and transporter " + quote.transporterId()
                );
            }

            toMinorUnits(quote.amount());
        }

        if (input.maxTransporters() < 1) {
            throw new OptimizationException(
                    "Maximum transporters must be at least 1"
            );
        }
    }

    private record QuoteKey(
            Long laneId,
            Long transporterId
    ) {
    }

    private record ModelVariables(
            Map<QuoteKey, BoolVar> assignmentVariables,
            Map<Long, BoolVar> transporterVariables,
            Map<QuoteKey, BigDecimal> quoteAmounts
    ) {
    }
}