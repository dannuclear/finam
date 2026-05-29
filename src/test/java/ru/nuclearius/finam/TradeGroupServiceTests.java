package ru.nuclearius.finam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import ru.nuclearius.finam.db.Asset;
import ru.nuclearius.finam.db.TradeGroup;
import ru.nuclearius.finam.db.TradeGroupReferenceAsset;
import ru.nuclearius.finam.db.TradeGroupTradedAsset;
import ru.nuclearius.finam.repository.AssetRepository;
import ru.nuclearius.finam.rest.dto.TradeGroupReferenceAssetDTO;
import ru.nuclearius.finam.rest.dto.TradeGroupTradedAssetDTO;
import ru.nuclearius.finam.service.TradeGroupService;
import ru.nuclearius.finam.service.meta.ChangeStatus;

@SpringBootTest
@ActiveProfiles("test")
public class TradeGroupServiceTests {
	@Autowired
	private TradeGroupService service;
	@Autowired
	private AssetRepository assetRepository;

	private Asset persistAsset(String id, String symbol, String ticker, String mic) {
		return assetRepository.save(Asset.builder()
				.id(id)
				.symbol(symbol)
				.ticker(ticker)
				.mic(mic)
				.build());
	}

	@Test
	@Transactional
	void shoudPersistTradeGroupWithTradedAssets() {
		assertEquals(assetRepository.count(), 0);
		List<TradeGroupTradedAssetDTO> tradedAssets = Stream.iterate(0, n -> n + 1)
				.takeWhile(n -> n < 10)
				.map(n -> persistAsset("ID-" + n, "SYMBOL-" + n, "TICKER-" + n, "MIC-" + n))
				.map(asset -> TradeGroupTradedAssetDTO.builder().asset(asset).build())
				.peek(dto -> dto.setChangeStatus(ChangeStatus.NEW))
				.toList();

		TradeGroup tradeGroup = service.create("TEST_GROUP",
				"TEST_DESCRIPTION",
				tradedAssets,
				null);

		assertNotNull(tradeGroup.getId());
		List<TradeGroupTradedAsset> savedTradedAssets = service.tradedAssetsByGroupId(tradeGroup.getId());
		assertEquals(savedTradedAssets.size(), tradedAssets.size());
	}

	@Test
	@Transactional
	void shoudPersistTradeGroupWithReferenceAssets() {
		assertEquals(assetRepository.count(), 0);
		List<TradeGroupReferenceAssetDTO> referenceAssets = Stream.iterate(0, n -> n + 1)
				.takeWhile(n -> n < 10)
				.map(n -> persistAsset("ID-" + n, "SYMBOL-" + n, "TICKER-" + n, "MIC-" + n))
				.map(asset -> TradeGroupReferenceAssetDTO.builder().asset(asset).build())
				.peek(dto -> dto.setChangeStatus(ChangeStatus.NEW))
				.toList();

		TradeGroup tradeGroup = service.create("TEST_GROUP",
				"TEST_DESCRIPTION",
				null,
				referenceAssets);

		assertNotNull(tradeGroup.getId());
		List<TradeGroupReferenceAsset> savedReferenceAssets = service.referenceAssetsByGroupId(tradeGroup.getId());
		assertEquals(savedReferenceAssets.size(), referenceAssets.size());
	}

	@Test
	void shoudThrowsValidationException() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.create(null, "TEST_DESCRIPTION", null, null);
		});
	}
}
