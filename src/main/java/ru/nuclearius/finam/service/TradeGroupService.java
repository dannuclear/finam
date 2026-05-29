package ru.nuclearius.finam.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.db.TradeGroup;
import ru.nuclearius.finam.db.TradeGroupReferenceAsset;
import ru.nuclearius.finam.db.TradeGroupTradedAsset;
import ru.nuclearius.finam.repository.TradeGroupReferenceAssetRepository;
import ru.nuclearius.finam.repository.TradeGroupRepository;
import ru.nuclearius.finam.repository.TradeGroupTradedAssetRepository;
import ru.nuclearius.finam.rest.dto.TradeGroupReferenceAssetDTO;
import ru.nuclearius.finam.rest.dto.TradeGroupTradedAssetDTO;
import ru.nuclearius.finam.service.exception.TradeGroupNotFoundException;
import ru.nuclearius.finam.service.mapper.EntityMapper;
import ru.nuclearius.finam.service.meta.MetaUtils;

@Service
@RequiredArgsConstructor
public class TradeGroupService {

    private final TradeGroupRepository repository;
    private final TradeGroupTradedAssetRepository tradedAssetRepository;
    private final TradeGroupReferenceAssetRepository referenceAssetRepository;
    private final EntityMapper entityMapper;

    public Page<TradeGroup> findAll(String q, Pageable pageable) {
        if (StringUtils.isBlank(q)) {
            return repository.findAll(pageable);
        }
        return repository.findByNameContainsIgnoreCase(q, pageable);
    }

    public TradeGroup getById(Integer id) {
        Assert.notNull(id, "Id должно быть указано");
        return repository.findById(id)
                .orElseThrow(() -> new TradeGroupNotFoundException(id));
    }

    @Transactional
    public TradeGroup create(
            String name,
            String description,
            List<TradeGroupTradedAssetDTO> tradeAssets,
            List<TradeGroupReferenceAssetDTO> referenceAssets) {

        Assert.hasText(name, "Наименование группы должно быть указано");

        TradeGroup saved = repository.save(
                TradeGroup.builder()
                        .name(name)
                        .description(description)
                        .build());

        MetaUtils.applyChanges(tradedAssetRepository, tradeAssets,
                (dto) -> entityMapper.toDomain(dto, saved), TradeGroupTradedAssetDTO::getId);

        MetaUtils.applyChanges(referenceAssetRepository, referenceAssets,
                (dto) -> entityMapper.toDomain(dto, saved), TradeGroupReferenceAssetDTO::getId);

        return saved;
    }

    @Transactional
    public TradeGroup update(
            Integer id,
            String name,
            String description,
            List<TradeGroupTradedAssetDTO> tradeAssets,
            List<TradeGroupReferenceAssetDTO> referenceAssets) {

        Assert.notNull(id, "Id должен быть указан");
        Assert.hasText(name, "Наименование группы должно быть указано");

        TradeGroup saved = repository.findById(id)
                .orElseThrow(() -> new TradeGroupNotFoundException(id));

        saved.setName(name);
        saved.setDescription(description);

        repository.save(saved);

        MetaUtils.applyChanges(tradedAssetRepository, tradeAssets,
                (dto) -> entityMapper.toDomain(dto, saved), TradeGroupTradedAssetDTO::getId);

        MetaUtils.applyChanges(referenceAssetRepository, referenceAssets,
                (dto) -> entityMapper.toDomain(dto, saved), TradeGroupReferenceAssetDTO::getId);

        return saved;
    }

    public List<TradeGroupTradedAsset> tradedAssetsByGroupId(Integer id) {
        Assert.notNull(id, "Id должен быть указан");
        return tradedAssetRepository.findByTradeGroupId(id);
    }

    public List<TradeGroupReferenceAsset> referenceAssetsByGroupId(Integer id) {
        Assert.notNull(id, "Id должен быть указан");
        return referenceAssetRepository.findByTradeGroupId(id);
    }

    public void enable(Integer id) {
        Assert.notNull(id, "Id должен быть указан");
        repository.enable(id);
    }

    public void disable(Integer id) {
        Assert.notNull(id, "Id должен быть указан");
        repository.disable(id);
    }
}
