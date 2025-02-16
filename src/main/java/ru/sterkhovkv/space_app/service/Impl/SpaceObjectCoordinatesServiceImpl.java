package ru.sterkhovkv.space_app.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;
import ru.sterkhovkv.space_app.dto.SkyEquatorialCoordinates;
import ru.sterkhovkv.space_app.dto.SkyHorizontalCoordinates;
import ru.sterkhovkv.space_app.model.SpaceObject;
import ru.sterkhovkv.space_app.service.SpaceObjectCoordinatesService;
import ru.sterkhovkv.space_app.service.SpaceObjectDataService;
import ru.sterkhovkv.space_app.util.SkyCoordinatesTranslator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpaceObjectCoordinatesServiceImpl implements SpaceObjectCoordinatesService {
    private final SpaceObjectDataService spaceObjectDataService;

    private final Map<Integer, SpaceObject> spaceStationCache = new ConcurrentHashMap<>();
    private final Map<Integer, SpaceObject> satelliteCache = new ConcurrentHashMap<>();

    @Override
    public void loadSpaceObjectsToCache(boolean spaceStation) {
        List<SpaceObject> spaceObjects = spaceObjectDataService.getSpaceObjectsFromDB(spaceStation);
        Map<Integer, SpaceObject> targetCache = spaceStation ? spaceStationCache : satelliteCache;

        for (SpaceObject spaceObject : spaceObjects) {
            targetCache.put(spaceObject.getNoradCatId(), spaceObject);
        }

        log.info("Loaded {} {} into cache.", spaceObjects.size(), spaceStation ? "Space Stations" : "Satellites");
    }

    @Override
    public List<SatelliteMapDTO> getSpaceObjectsList(EarthPositionCoordinates position, ZonedDateTime dateTime, boolean spaceStation) {
        return getSpaceObjects(position, dateTime, spaceStation, false);
    }

    @Override
    public List<SatelliteMapDTO> getVisibleSpaceObjectsList(EarthPositionCoordinates position, ZonedDateTime dateTime, boolean spaceStation) {
        return getSpaceObjects(position, dateTime, spaceStation, true);
    }

    private List<SatelliteMapDTO> getSpaceObjects(EarthPositionCoordinates position, ZonedDateTime dateTime, boolean spaceStation, boolean onlyVisible) {
        List<SatelliteMapDTO> spaceObjectCoordinates = new ArrayList<>();
        Map<Integer, SpaceObject> targetCache = spaceStation ? spaceStationCache : satelliteCache;

        if (targetCache.isEmpty()) {
            loadSpaceObjectsToCache(spaceStation);
        }

        targetCache.values().forEach(spaceObject -> {
            SkyEquatorialCoordinates equatorialCoordinates = SkyCoordinatesTranslator.calculateEquatorialCoordinates(spaceObject, position, dateTime);
            SkyHorizontalCoordinates horizontalCoordinates = SkyCoordinatesTranslator.calculateSkyHorizontalCoordinates(equatorialCoordinates, position, dateTime);

            boolean isVisible = spaceObject.getVisible() && (horizontalCoordinates.getAlt() > 0);
            if (!onlyVisible || isVisible) {
                SatelliteMapDTO satelliteMapDTO = SatelliteMapDTO.builder()
                        .coordinates(horizontalCoordinates)
                        .objectId(spaceObject.getNoradCatId())
                        .objectName(spaceObject.getObjectName())
                        .visible(spaceObject.getVisible())
                        .build();
                spaceObjectCoordinates.add(satelliteMapDTO);
            }
        });

        log.info("Loaded {} {} from cache", spaceObjectCoordinates.size(), spaceStation ? "Space Stations" : "Satellites");
        return spaceObjectCoordinates;
    }

    @Override
    public void invalidateCache(boolean spaceStation) {
        if (spaceStation) {
            spaceStationCache.clear();
            log.info("Space Station Cache has been invalidated.");
        } else {
            satelliteCache.clear();
            log.info("Satellite Cache has been invalidated.");
        }
    }
}
