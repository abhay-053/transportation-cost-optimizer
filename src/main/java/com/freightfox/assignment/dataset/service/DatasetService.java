package com.freightfox.assignment.dataset.service;

import com.freightfox.assignment.admin.entity.AdminUser;
import com.freightfox.assignment.admin.repository.AdminUserRepository;

import com.freightfox.assignment.dataset.dto.*;
import com.freightfox.assignment.dataset.entity.*;
import com.freightfox.assignment.dataset.repository.*;

import com.freightfox.assignment.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatasetService {

    private final AdminUserRepository adminUserRepository;
    private final InputDatasetRepository datasetRepository;
    private final LaneRepository laneRepository;
    private final TransporterRepository transporterRepository;
    private final LaneQuoteRepository laneQuoteRepository;
    private final DatasetValidationService validationService;

    public DatasetService(
            AdminUserRepository adminUserRepository,
            InputDatasetRepository datasetRepository,
            LaneRepository laneRepository,
            TransporterRepository transporterRepository,
            LaneQuoteRepository laneQuoteRepository,
            DatasetValidationService validationService) {

        this.adminUserRepository = adminUserRepository;
        this.datasetRepository = datasetRepository;
        this.laneRepository = laneRepository;
        this.transporterRepository = transporterRepository;
        this.laneQuoteRepository = laneQuoteRepository;
        this.validationService = validationService;
    }

    @Transactional
    public InputDataResponse saveInputData(
            InputDataRequest request,
            String adminEmail) {

        validationService.validate(request);

        AdminUser admin = getAdmin(adminEmail);

        String datasetName = "Dataset-" +
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                );

        InputDataset dataset =
                datasetRepository.save(new InputDataset(admin, datasetName));

        List<Lane> lanes = request.lanes().stream()
                .map(input -> new Lane(
                        dataset,
                        String.valueOf(input.id()),
                        input.origin().trim(),
                        input.destination().trim()
                ))
                .toList();

        List<Lane> savedLanes = laneRepository.saveAll(lanes);

        Map<Long, Lane> laneMap = new HashMap<>();

        savedLanes.forEach(lane ->
                laneMap.put(Long.parseLong(lane.getLaneCode()), lane)
        );

        for (var input : request.transporters()) {

            Transporter transporter = transporterRepository.save(
                    new Transporter(
                            dataset,
                            String.valueOf(input.id()),
                            input.name().trim()
                    )
            );

            List<LaneQuote> quotes = input.laneQuotes().stream()
                    .map(quote -> new LaneQuote(
                            laneMap.get(quote.laneId()),
                            transporter,
                            quote.quote()
                    ))
                    .toList();

            laneQuoteRepository.saveAll(quotes);
        }

        return new InputDataResponse(
                "success",
                "Input data saved successfully."
        );
    }

    @Transactional(readOnly = true)
    public List<DatasetResponse> getAllDatasets(String adminEmail) {

        AdminUser admin = getAdmin(adminEmail);

        return datasetRepository
                .findAllByAdminIdOrderByCreatedAtDesc(admin.getId())
                .stream()
                .map(this::toDatasetResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DatasetResponse getDataset(
            Long datasetId,
            String adminEmail) {

        InputDataset dataset = getOwnedDataset(datasetId, adminEmail);

        return toDatasetResponse(dataset);
    }

    @Transactional(readOnly = true)
    public List<LaneResponse> getDatasetLanes(
            Long datasetId,
            String adminEmail) {

        getOwnedDataset(datasetId, adminEmail);

        return laneRepository.findAllByDatasetId(datasetId)
                .stream()
                .map(lane -> new LaneResponse(
                        Long.parseLong(lane.getLaneCode()),
                        lane.getOrigin(),
                        lane.getDestination()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransporterResponse> getDatasetTransporters(
            Long datasetId,
            String adminEmail) {

        getOwnedDataset(datasetId, adminEmail);

        List<Transporter> transporters =
                transporterRepository.findAllByDatasetId(datasetId);

        List<LaneQuote> quotes =
                laneQuoteRepository.findAllByLane_Dataset_Id(datasetId);

        Map<Long, List<QuoteResponse>> quotesByTransporter =
                quotes.stream().collect(Collectors.groupingBy(
                        quote -> quote.getTransporter().getId(),
                        Collectors.mapping(
                                quote -> new QuoteResponse(
                                        Long.parseLong(quote.getLane().getLaneCode()),
                                        quote.getQuoteAmount()
                                ),
                                Collectors.toList()
                        )
                ));

        return transporters.stream()
                .map(transporter -> new TransporterResponse(
                        Long.parseLong(transporter.getTransporterCode()),
                        transporter.getTransporterName(),
                        quotesByTransporter.getOrDefault(
                                transporter.getId(),
                                List.of()
                        )
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public InputDataset getLatestDataset(String adminEmail) {

        AdminUser admin = getAdmin(adminEmail);

        return datasetRepository
                .findFirstByAdminIdOrderByCreatedAtDesc(admin.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No dataset found. Please upload input data first."
                        ));
    }

    private AdminUser getAdmin(String email) {

        return adminUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Admin not found"));
    }

    private InputDataset getOwnedDataset(
            Long datasetId,
            String adminEmail) {

        AdminUser admin = getAdmin(adminEmail);

        return datasetRepository
                .findByIdAndAdminId(datasetId, admin.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Dataset not found"
                        ));
    }

    private DatasetResponse toDatasetResponse(InputDataset dataset) {

        return new DatasetResponse(
                dataset.getId(),
                dataset.getDatasetName(),
                dataset.getCreatedAt(),
                laneRepository.countByDatasetId(dataset.getId()),
                transporterRepository.countByDatasetId(dataset.getId())
        );
    }
}