package com.freightfox.assignment.assignment.entity;

import com.freightfox.assignment.admin.entity.AdminUser;
import com.freightfox.assignment.dataset.entity.InputDataset;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_run")
public class AssignmentRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private AdminUser admin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false)
    private InputDataset dataset;

    @Column(name = "max_transporters", nullable = false)
    private Integer maxTransporters;

    @Column(name = "selected_transporter_count", nullable = false)
    private Integer selectedTransporterCount;

    @Column(name = "total_cost", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "solver_status", nullable = false, length = 30)
    private String solverStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AssignmentRun() {
    }

    public AssignmentRun(
            AdminUser admin,
            InputDataset dataset,
            Integer maxTransporters,
            Integer selectedTransporterCount,
            BigDecimal totalCost,
            String solverStatus) {

        this.admin = admin;
        this.dataset = dataset;
        this.maxTransporters = maxTransporters;
        this.selectedTransporterCount = selectedTransporterCount;
        this.totalCost = totalCost;
        this.solverStatus = solverStatus;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public AdminUser getAdmin() {
        return admin;
    }

    public InputDataset getDataset() {
        return dataset;
    }

    public Integer getMaxTransporters() {
        return maxTransporters;
    }

    public Integer getSelectedTransporterCount() {
        return selectedTransporterCount;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public String getSolverStatus() {
        return solverStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}