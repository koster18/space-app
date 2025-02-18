package ru.sterkhovkv.space_app.service.Impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.dto.RectangularCoordinates;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;
import ru.sterkhovkv.space_app.dto.SkyEquatorialCoordinates;
import ru.sterkhovkv.space_app.dto.SkyHorizontalCoordinates;
import ru.sterkhovkv.space_app.enums.CalculationMode;
import ru.sterkhovkv.space_app.model.SpaceObject;
import ru.sterkhovkv.space_app.service.SpaceObjectCoordinatesService;
import ru.sterkhovkv.space_app.service.SpaceObjectDataService;
import ru.sterkhovkv.space_app.sgp4.TLE;
import ru.sterkhovkv.space_app.util.SkyCoordinatesTranslator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpaceObjectCoordinatesServiceImpl implements SpaceObjectCoordinatesService {
    private final @Lazy SpaceObjectDataService spaceObjectDataService;

    private final Map<Integer, CacheSpaceStation> spaceStationCache = new ConcurrentHashMap<>();
    private final Map<Integer, CacheSpaceStation> satelliteCache = new ConcurrentHashMap<>();

    @Override
    public void loadSpaceObjectsToCache(boolean spaceStation) {
        List<SpaceObject> spaceObjects = spaceObjectDataService.getSpaceObjectsFromDB(spaceStation);
        Map<Integer, CacheSpaceStation> targetCache = spaceStation ? spaceStationCache : satelliteCache;

        for (SpaceObject spaceObject : spaceObjects) {
            CacheSpaceStation cacheSpaceStation = new CacheSpaceStation();
            cacheSpaceStation.setSpaceObject(spaceObject);
            if (spaceObject.getTleLine1() != null && spaceObject.getTleLine2() != null) {
                cacheSpaceStation.setTle(new TLE(spaceObject.getTleLine1(), spaceObject.getTleLine2()));
            }
            targetCache.put(spaceObject.getNoradCatId(), cacheSpaceStation);
        }

        log.info("Loaded {} {} into cache.", targetCache.size(), spaceStation ? "Space Stations" : "Satellites");
    }

    @Override
    public List<SatelliteMapDTO> getSpaceObjectsList(EarthPositionCoordinates position, ZonedDateTime dateTime,
                                                     CalculationMode calculationMode, boolean spaceStation) {
        return getSpaceObjects(position, dateTime, calculationMode, spaceStation, false);
    }

    @Override
    public List<SatelliteMapDTO> getSpaceObjectsList(EarthPositionCoordinates position, ZonedDateTime dateTime, boolean spaceStation) {
        return getSpaceObjects(position, dateTime, CalculationMode.KEPLER_NEWTON, spaceStation, false);
    }

    @Override
    public List<SatelliteMapDTO> getVisibleSpaceObjectsList(EarthPositionCoordinates position, ZonedDateTime dateTime,
                                                            CalculationMode calculationMode, boolean spaceStation) {
        return getSpaceObjects(position, dateTime, calculationMode, spaceStation, true);
    }

    @Override
    public List<SatelliteMapDTO> getVisibleSpaceObjectsList(EarthPositionCoordinates position, ZonedDateTime dateTime, boolean spaceStation) {
        return getSpaceObjects(position, dateTime, CalculationMode.KEPLER_NEWTON, spaceStation, true);
    }

    private List<SatelliteMapDTO> getSpaceObjects(EarthPositionCoordinates position, ZonedDateTime dateTime,
                                                  CalculationMode calculationMode, boolean spaceStation, boolean onlyVisible) {
        List<SatelliteMapDTO> spaceObjectCoordinates = new ArrayList<>();
        Map<Integer, CacheSpaceStation> targetCache = spaceStation ? spaceStationCache : satelliteCache;

        if (targetCache.isEmpty()) {
            loadSpaceObjectsToCache(spaceStation);
        }

        targetCache.values().forEach(cacheSpaceStation -> {
            SkyEquatorialCoordinates equatorialCoordinates;

            if (calculationMode == CalculationMode.SGP4 && cacheSpaceStation.getTle() != null) {
                double[][] rv = cacheSpaceStation.getTle().getRV(Date.from(dateTime.toInstant()));
                equatorialCoordinates = SkyCoordinatesTranslator.calculateEquatorialCoordinates(
                        new RectangularCoordinates(rv[0][0], rv[0][1], rv[0][2]), position, dateTime);
            } else {
                equatorialCoordinates = SkyCoordinatesTranslator.calculateEquatorialCoordinates(
                        cacheSpaceStation.getSpaceObject(), position, dateTime);
            }

            SkyHorizontalCoordinates horizontalCoordinates = SkyCoordinatesTranslator.calculateSkyHorizontalCoordinates(
                    equatorialCoordinates, position, dateTime);

            boolean isVisible = cacheSpaceStation.getSpaceObject().getVisible() && (horizontalCoordinates.getAlt() > 0);
            if (!onlyVisible || isVisible) {
                SatelliteMapDTO satelliteMapDTO = SatelliteMapDTO.builder()
                        .coordinates(horizontalCoordinates)
                        .objectId(cacheSpaceStation.getSpaceObject().getNoradCatId())
                        .objectName(cacheSpaceStation.getSpaceObject().getObjectName())
                        .visible(cacheSpaceStation.getSpaceObject().getVisible())
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class CacheSpaceStation {
        private SpaceObject spaceObject;
        private TLE tle;
    }
}
