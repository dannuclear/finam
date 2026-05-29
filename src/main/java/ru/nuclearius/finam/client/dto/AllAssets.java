package ru.nuclearius.finam.client.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import ru.nuclearius.finam.db.Asset;

@Getter
@Setter
public class AllAssets {
    private List<Asset> assets;
}
