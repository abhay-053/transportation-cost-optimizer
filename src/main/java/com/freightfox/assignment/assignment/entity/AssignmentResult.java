package com.freightfox.assignment.assignment.entity;

import com.freightfox.assignment.dataset.entity.Lane;
import com.freightfox.assignment.dataset.entity.Transporter;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "assignment_result")
public class AssignmentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_run_id", nullable = false)
    private AssignmentRun assignmentRun;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lane_id", nullable = false)
    private Lane lane;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transporter_id", nullable = false)
    private Transporter transporter;

    @Column(name = "quote_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal quoteAmount;

    protected AssignmentResult() {
    }

    public AssignmentResult(
            AssignmentRun assignmentRun,
            Lane lane,
            Transporter transporter,
            BigDecimal quoteAmount) {

        this.assignmentRun = assignmentRun;
        this.lane = lane;
        this.transporter = transporter;
        this.quoteAmount = quoteAmount;
    }

    public Long getId() {
        return id;
    }

    public AssignmentRun getAssignmentRun() {
        return assignmentRun;
    }

    public Lane getLane() {
        return lane;
    }

    public Transporter getTransporter() {
        return transporter;
    }

    public BigDecimal getQuoteAmount() {
        return quoteAmount;
    }
}