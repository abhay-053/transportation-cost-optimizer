package com.freightfox.assignment.assignment.repository;

import com.freightfox.assignment.assignment.entity.AssignmentRun;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRunRepository
        extends JpaRepository<AssignmentRun, Long> {

    List<AssignmentRun> findAllByAdminIdOrderByCreatedAtDesc(Long adminId);

    Optional<AssignmentRun> findByIdAndAdminId(Long id, Long adminId);

    List<AssignmentRun> findAllByDatasetIdAndAdminIdOrderByCreatedAtDesc(
            Long datasetId,
            Long adminId
    );
}