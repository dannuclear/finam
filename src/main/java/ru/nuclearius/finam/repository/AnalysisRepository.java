package ru.nuclearius.finam.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import ru.nuclearius.finam.db.Analysis;

@Transactional(readOnly = true)
public interface AnalysisRepository extends JpaRepository<Analysis, Integer> {

    Page<Analysis> findByNameContainsIgnoreCase(String name, Pageable pageable);
}
