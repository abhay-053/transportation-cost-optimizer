package com.freightfox.assignment.dataset.repository;

import com.freightfox.assignment.dataset.entity.LaneQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaneQuoteRepository
        extends JpaRepository<LaneQuote, Long> {

    List<LaneQuote> findAllByLaneDatasetId(Long datasetId);
}