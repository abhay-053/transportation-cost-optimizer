package com.freightfox.assignment.dataset.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "lane_quote",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_lane_transporter_quote",
                        columnNames = {"lane_id", "transporter_id"}
                )
        }
)
public class LaneQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lane_id", nullable = false)
    private Lane lane;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transporter_id", nullable = false)
    private Transporter transporter;

    @Column(
            name = "quote_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal quoteAmount;

    protected LaneQuote() {
    }

    public LaneQuote(
            Lane lane,
            Transporter transporter,
            BigDecimal quoteAmount) {

        this.lane = lane;
        this.transporter = transporter;
        this.quoteAmount = quoteAmount;
    }

    public Long getId() {
        return id;
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