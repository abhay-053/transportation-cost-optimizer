package com.freightfox.assignment.dataset.repository;

import com.freightfox.assignment.dataset.entity.Lane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaneRepository extends JpaRepository<Lane, Long> {

    List<Lane> findAllByDatasetId(Long datasetId);

    long countByDatasetId(Long datasetId);
}