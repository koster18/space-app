package ru.sterkhovkv.space_app.service;

import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;

import java.time.ZonedDateTime;
import java.util.List;

public interface SpaceStationConfigurationService {
    List<SatelliteMapDTO> getSpaceStationsList(EarthPositionCoordinates position, ZonedDateTime dateTime);

    List<SatelliteMapDTO> updateSpaceStationsList(EarthPositionCoordinates position, ZonedDateTime dateTime);

    boolean saveSpaceStationProperties(List<SatelliteMapDTO> satellites);
}
