package com.freightfox.assignment.dataset.service;

import com.freightfox.assignment.dataset.dto.InputDataRequest;
import com.freightfox.assignment.dataset.dto.TransporterInputRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@Service
public class DatasetValidationService {

    public void validate(InputDataRequest request) {

        Set<Long> laneIds = new HashSet<>();

        request.lanes().forEach(lane -> {
            if (!laneIds.add(lane.id())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate lane ID: " + lane.id()
                );
            }
        });

        Set<Long> transporterIds = new HashSet<>();

        for (TransporterInputRequest transporter : request.transporters()) {

            if (!transporterIds.add(transporter.id())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate transporter ID: " + transporter.id()
                );
            }

            Set<Long> quotedLaneIds = new HashSet<>();

            for (var quote : transporter.laneQuotes()) {

                if (!laneIds.contains(quote.laneId())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Quote references unknown lane ID: " + quote.laneId()
                    );
                }

                if (!quotedLaneIds.add(quote.laneId())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Duplicate quote for lane " + quote.laneId()
                                    + " by transporter " + transporter.id()
                    );
                }
            }
        }
    }
}