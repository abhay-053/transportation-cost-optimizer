package com.freightfox.assignment.dataset.entity;

import com.freightfox.assignment.admin.entity.AdminUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "input_dataset")
public class InputDataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private AdminUser admin;

    @Column(name = "dataset_name", nullable = false, length = 150)
    private String datasetName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected InputDataset() {
    }

    public InputDataset(AdminUser admin, String datasetName) {
        this.admin = admin;
        this.datasetName = datasetName;
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

    public String getDatasetName() {
        return datasetName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setAdmin(AdminUser admin) {
        this.admin = admin;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }
}