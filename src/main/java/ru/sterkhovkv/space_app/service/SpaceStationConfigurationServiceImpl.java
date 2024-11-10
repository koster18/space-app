package ru.sterkhovkv.space_app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;
import ru.sterkhovkv.space_app.dto.SkyEquatorialCoordinates;
import ru.sterkhovkv.space_app.dto.SkyHorizontalCoordinates;
import ru.sterkhovkv.space_app.model.Satellite;
import ru.sterkhovkv.space_app.model.SpaceStation;
import ru.sterkhovkv.space_app.util.SkyCoordinatesTranslator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpaceStationConfigurationServiceImpl implements SpaceStationConfigurationService {
    private final SpaceStationDBService spaceStationDBService;

    private List<SatelliteMapDTO> spaceStationList;

    @Override
    public List<SatelliteMapDTO> getSpaceStationsList(EarthPositionCoordinates position, ZonedDateTime dateTime) {
        if (spaceStationList == null) {
            spaceStationList = updateSpaceStationsList(position, dateTime);
        } else if (spaceStationList.isEmpty()) {
            spaceStationList = updateSpaceStationsList(position, dateTime);
        }
        return spaceStationList;
    }

    @Override
    public List<SatelliteMapDTO> updateSpaceStationsList(EarthPositionCoordinates position, ZonedDateTime dateTime) {
        List<SatelliteMapDTO> satelliteCoordinates = new ArrayList<>();

        List<SpaceStation> spaceStations = spaceStationDBService.getSpaceStationsFromDB();
        if (!spaceStations.isEmpty()) {
            for (SpaceStation spaceStation : spaceStations) {
                SkyEquatorialCoordinates equatorialCoordinates = SkyCoordinatesTranslator.calculateEquatorialCoordinates(
                        Satellite.fromSpaceStation(spaceStation), position, dateTime);
                SkyHorizontalCoordinates horizontalCoordinates = SkyCoordinatesTranslator.calculateSkyHorizontalCoordinates(
                        equatorialCoordinates, position, dateTime);
                SatelliteMapDTO satelliteMapDTO = new SatelliteMapDTO();
                satelliteMapDTO.setCoordinates(horizontalCoordinates);
                satelliteMapDTO.setObjectId(spaceStation.getNoradCatId());
                satelliteMapDTO.setObjectName(spaceStation.getObjectName());
                satelliteMapDTO.setVisible(spaceStation.getVisible());
                satelliteCoordinates.add(satelliteMapDTO);
            }
        } else log.info("Satellites DB empty");
        return satelliteCoordinates;
    }

    @Override
    public boolean saveSpaceStationProperties(List<SatelliteMapDTO> satellites) {
        boolean result = true;
        for (SatelliteMapDTO satellite : satellites) {
            result = result &&
            spaceStationDBService.setSpaceStationVisible(satellite.getObjectId(), satellite.getVisible());
        }
        return result;
    }
}
