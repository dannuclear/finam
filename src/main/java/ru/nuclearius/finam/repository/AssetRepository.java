package ru.nuclearius.finam.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import ru.nuclearius.finam.db.Asset;

@Transactional(readOnly = true)
public interface AssetRepository extends JpaRepository<Asset, String> {

    List<Asset> findAllBySymbolIn(Iterable<String> symbols);

    @Query("""
                SELECT a
                FROM Asset a
                WHERE LOWER(a.ticker) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(a.name)   LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<Asset> search(
            @Param("q") String q,
            Pageable pageable);
}
