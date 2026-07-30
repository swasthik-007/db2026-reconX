package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.repository.entity.ReconResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReconResultRepository 
        extends JpaRepository<ReconResultEntity, Long> {
}