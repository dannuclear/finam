package ru.nuclearius.finam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.nuclearius.finam.db.AnalysisAsset;

@Repository
public interface AnalysisAssetRepository extends JpaRepository<AnalysisAsset, Integer> {

    @EntityGraph(attributePaths = { "asset" })
    List<AnalysisAsset> findByAnalysisId(Integer tradeGroupId);
}