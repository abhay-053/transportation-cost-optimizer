package com.freightfox.assignment.dataset.repository;

import com.freightfox.assignment.dataset.entity.Transporter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransporterRepository
        extends JpaRepository<Transporter, Long> {

    List<Transporter> findAllByDatasetId(Long datasetId);

    long countByDatasetId(Long datasetId);
}