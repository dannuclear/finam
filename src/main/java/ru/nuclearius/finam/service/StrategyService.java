package ru.nuclearius.finam.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.db.Strategy;
import ru.nuclearius.finam.db.StrategyAsset;
import ru.nuclearius.finam.repository.StrategyAssetRepository;
import ru.nuclearius.finam.repository.StrategyRepository;
import ru.nuclearius.finam.repository.specs.CommonSpec;
import ru.nuclearius.finam.rest.dto.StrategyAssetDto;
import ru.nuclearius.finam.service.exception.StrategyNotFoundException;
import ru.nuclearius.finam.service.mapper.EntityMapper;
import ru.nuclearius.finam.service.meta.MetaUtils;

@Service
@RequiredArgsConstructor
public class StrategyService {
    private final StrategyRepository strategyRepository;
    private final StrategyAssetRepository strategyAssetRepository;
    private final EntityMapper entityMapper;

    public Page<Strategy> findAll(String q, Pageable pageable) {
        return strategyRepository.findAll(CommonSpec.byQueryOfName(q), pageable);
    }

    public Strategy getById(Integer id) {
        Assert.notNull(id, "Id должно быть указано");
        return strategyRepository.findById(id)
                .orElseThrow(() -> new StrategyNotFoundException(id));
    }

    public List<StrategyAsset> assets(Integer strategyId) {
        return strategyAssetRepository.findByStrategyId(strategyId);
    }

    @Transactional
    public Strategy create(
            String name,
            List<StrategyAssetDto> assets) {

        Assert.hasText(name, "Наименование стратегии быть указано");

        Strategy saved = strategyRepository.save(
                Strategy.builder()
                        .name(name)
                        .build());

        MetaUtils.applyChanges(strategyAssetRepository, assets,
                (dto) -> entityMapper.toDomain(dto, saved), StrategyAssetDto::getId);

        return saved;
    }

    @Transactional
    public Strategy update(
            Integer id,
            String name,
            List<StrategyAssetDto> assets) {

        Assert.notNull(id, "Id должен быть указан");
        Assert.hasText(name, "Наименование группы должно быть указано");

        Strategy saved = strategyRepository.findById(id)
                .orElseThrow(() -> new StrategyNotFoundException(id));

        saved.setName(name);

        strategyRepository.save(saved);

        MetaUtils.applyChanges(strategyAssetRepository, assets,
                (dto) -> entityMapper.toDomain(dto, saved), StrategyAssetDto::getId);

        return saved;
    }

    public void delete(Integer id) {
        Assert.notNull(id, "Id должен быть указан");
        strategyRepository.deleteById(id);
    }
}
