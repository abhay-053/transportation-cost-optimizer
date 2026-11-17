package com.freightfox.assignment.dataset.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "lane",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_lane_dataset_code",
                        columnNames = {"dataset_id", "lane_code"}
                )
        }
)
public class Lane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false)
    private InputDataset dataset;

    @Column(name = "lane_code", nullable = false, length = 100)
    private String laneCode;

    @Column(nullable = false, length = 150)
    private String origin;

    @Column(nullable = false, length = 150)
    private String destination;

    protected Lane() {
    }

    public Lane(
            InputDataset dataset,
            String laneCode,
            String origin,
            String destination) {

        this.dataset = dataset;
        this.laneCode = laneCode;
        this.origin = origin;
        this.destination = destination;
    }

    public Long getId() {
        return id;
    }

    public InputDataset getDataset() {
        return dataset;
    }

    public String getLaneCode() {
        return laneCode;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }
}