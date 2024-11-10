package ru.sterkhovkv.space_app.service;

import ru.sterkhovkv.space_app.dto.ConstellationDTO;
import ru.sterkhovkv.space_app.dto.StarCatalogDTO;

import java.util.List;

public interface StarCatalogLoader {
    List<StarCatalogDTO> loadStarsFromFile();
    List<ConstellationDTO> loadConstellationsFromFile();
}
