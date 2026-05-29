package ru.nuclearius.finam.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ru.nuclearius.finam.db.TradeGroup;

@Repository
@Transactional(readOnly = true)
public interface TradeGroupRepository extends JpaRepository<TradeGroup, Integer> {

    Page<TradeGroup> findByNameContainsIgnoreCase(String name, Pageable pageable);

    @Modifying
    @Transactional(readOnly = false)
    @Query("UPDATE TradeGroup SET active = true WHERE id = ?1")
    void enable(Integer id);

    @Modifying
    @Transactional(readOnly = false)
    @Query("UPDATE TradeGroup SET active = false WHERE id = ?1")
    void disable(Integer id);
}