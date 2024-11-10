package ru.sterkhovkv.space_app.service;

import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;

import java.time.ZonedDateTime;
import java.util.List;

public interface SatelliteConfigurationService {
    List<SatelliteMapDTO> getSatelliteList(EarthPositionCoordinates position, ZonedDateTime dateTime);

    List<SatelliteMapDTO> updateSatelliteList(EarthPositionCoordinates position, ZonedDateTime dateTime);

    boolean saveSatelliteProperties(List<SatelliteMapDTO> satellites);
}
