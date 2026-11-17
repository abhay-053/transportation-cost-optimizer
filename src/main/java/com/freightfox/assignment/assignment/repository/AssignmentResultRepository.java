package com.freightfox.assignment.assignment.repository;

import com.freightfox.assignment.assignment.entity.AssignmentResult;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentResultRepository
        extends JpaRepository<AssignmentResult, Long> {

    List<AssignmentResult> findAllByAssignmentRunId(Long assignmentRunId);
}