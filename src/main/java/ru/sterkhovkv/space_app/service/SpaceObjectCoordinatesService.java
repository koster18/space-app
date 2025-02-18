package ru.sterkhovkv.space_app.service;

import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;
import ru.sterkhovkv.space_app.enums.CalculationMode;

import java.time.ZonedDateTime;
import java.util.List;

public interface SpaceObjectCoordinatesService {
    void loadSpaceObjectsToCache(boolean spaceStation);

    List<SatelliteMapDTO> getSpaceObjectsList(EarthPositionCoordinates position,ZonedDateTime dateTime,
                                              CalculationMode calculationMode, boolean spaceStation);

    List<SatelliteMapDTO> getSpaceObjectsList(EarthPositionCoordinates position,ZonedDateTime dateTime, boolean spaceStation);

    List<SatelliteMapDTO> getVisibleSpaceObjectsList(EarthPositionCoordinates position, ZonedDateTime dateTime,
                                                     CalculationMode calculationMode, boolean spaceStation);

    List<SatelliteMapDTO> getVisibleSpaceObjectsList(EarthPositionCoordinates position, ZonedDateTime dateTime, boolean spaceStation);

    void invalidateCache(boolean spaceStation);
}
