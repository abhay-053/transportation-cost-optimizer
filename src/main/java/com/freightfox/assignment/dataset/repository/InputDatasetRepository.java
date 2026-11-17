package com.freightfox.assignment.dataset.repository;

import com.freightfox.assignment.dataset.entity.InputDataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InputDatasetRepository
        extends JpaRepository<InputDataset, Long> {

    List<InputDataset> findAllByAdminIdOrderByCreatedAtDesc(Long adminId);

    Optional<InputDataset> findByIdAndAdminId(Long id, Long adminId);
}