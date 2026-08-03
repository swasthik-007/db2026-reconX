package com.dbtraining.reconx.repository.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "recon_results")
public class ReconResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tradeRef;

    private String status;

    public ReconResultEntity() {}

    public ReconResultEntity(String tradeRef, String status) {
        this.tradeRef = tradeRef;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getTradeRef() {
        return tradeRef;
    }

    public String getStatus() {
        return status;
    }
}