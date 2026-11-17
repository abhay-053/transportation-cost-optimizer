package com.freightfox.assignment.dataset.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "transporter",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transporter_dataset_code",
                        columnNames = {"dataset_id", "transporter_code"}
                )
        }
)
public class Transporter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false)
    private InputDataset dataset;

    @Column(name = "transporter_code", nullable = false, length = 100)
    private String transporterCode;

    @Column(name = "transporter_name", nullable = false, length = 150)
    private String transporterName;

    protected Transporter() {
    }

    public Transporter(
            InputDataset dataset,
            String transporterCode,
            String transporterName) {

        this.dataset = dataset;
        this.transporterCode = transporterCode;
        this.transporterName = transporterName;
    }

    public Long getId() {
        return id;
    }

    public InputDataset getDataset() {
        return dataset;
    }

    public String getTransporterCode() {
        return transporterCode;
    }

    public String getTransporterName() {
        return transporterName;
    }
}