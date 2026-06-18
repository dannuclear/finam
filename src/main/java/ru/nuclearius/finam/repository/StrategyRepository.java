package ru.nuclearius.finam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ru.nuclearius.finam.db.Strategy;

public interface StrategyRepository extends JpaRepository<Strategy, Integer>, JpaSpecificationExecutor<Strategy> {

}
