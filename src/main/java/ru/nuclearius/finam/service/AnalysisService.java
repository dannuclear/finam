package ru.nuclearius.finam.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import lombok.RequiredArgsConstructor;
import ru.nuclearius.finam.db.Analysis;
import ru.nuclearius.finam.db.AnalysisAsset;
import ru.nuclearius.finam.repository.AnalysisAssetRepository;
import ru.nuclearius.finam.repository.AnalysisRepository;
import ru.nuclearius.finam.rest.dto.AnalysisAssetDTO;
import ru.nuclearius.finam.service.exception.StrategyNotFoundException;
import ru.nuclearius.finam.service.mapper.EntityMapper;
import ru.nuclearius.finam.service.meta.MetaUtils;

@Service
@RequiredArgsConstructor
public class AnalysisService {
	private final AnalysisRepository analysisRepository;
	private final AnalysisAssetRepository analysisAssetRepository;
	private final EntityMapper entityMapper;

	public Page<Analysis> findAll(String q, Pageable pageable) {
		if (StringUtils.isBlank(q)) {
			return analysisRepository.findAll(pageable);
		}
		return analysisRepository.findByNameContainsIgnoreCase(q, pageable);
	}

	public Analysis getById(Integer id) {
		Assert.notNull(id, "Id должно быть указано");
		return analysisRepository.findById(id)
				.orElseThrow(() -> new StrategyNotFoundException(id));
	}

	public List<AnalysisAsset> assets(Integer analysisId) {
		Assert.notNull(analysisId, "Id должен быть указан");
		return analysisAssetRepository.findByAnalysisId(analysisId);
	}

	@Transactional
	public Analysis create(
			String name,
			List<AnalysisAssetDTO> assets,
			Integer averageDays) {

		Assert.hasText(name, "Наименование группы должно быть указано");

		Analysis saved = analysisRepository.save(
				Analysis.builder()
						.name(name)
						.averageDays(averageDays)
						.build());

		MetaUtils.applyChanges(analysisAssetRepository, assets,
				(dto) -> entityMapper.toDomain(dto, saved), AnalysisAssetDTO::getId);

		return saved;
	}

	@Transactional
	public Analysis update(
			Integer id,
			String name,
			List<AnalysisAssetDTO> assets,
			Integer averageDays) {

		Assert.notNull(id, "Id должен быть указан");
		Assert.hasText(name, "Наименование группы должно быть указано");

		Analysis saved = analysisRepository.findById(id)
				.orElseThrow(() -> new StrategyNotFoundException(id));

		saved.setName(name);
		saved.setAverageDays(averageDays);

		analysisRepository.save(saved);

		MetaUtils.applyChanges(analysisAssetRepository, assets,
				(dto) -> entityMapper.toDomain(dto, saved), AnalysisAssetDTO::getId);

		return saved;
	}

	public void delete(Integer id) {
		Assert.notNull(id, "Id должен быть указан");
		analysisRepository.deleteById(id);
	}
}
