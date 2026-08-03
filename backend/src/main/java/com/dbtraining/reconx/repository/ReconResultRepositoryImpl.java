package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.repository.entity.ReconResultEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReconResultRepositoryImpl implements ReconResultRepository {

    private final JpaReconResultRepository jpaRepository;

    public ReconResultRepositoryImpl(
            JpaReconResultRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }


    @Override
    public void save(ReconResult result) {

        ReconResultEntity entity =
                new ReconResultEntity(
                        result.tradeRef(),
                        result.status().name()
                );

        jpaRepository.save(entity);
    }


    @Override
    public List<ReconResult> findAll() {

        return jpaRepository.findAll()
                .stream()
                .map(e ->
                        new ReconResult(
                                e.getTradeRef(),
                                ReconResult.Status.valueOf(e.getStatus()),
                                null,
                                null
                        )
                )
                .toList();
    }
}