package ru.sterkhovkv.space_app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;
import ru.sterkhovkv.space_app.dto.SkyEquatorialCoordinates;
import ru.sterkhovkv.space_app.dto.SkyHorizontalCoordinates;
import ru.sterkhovkv.space_app.model.Satellite;
import ru.sterkhovkv.space_app.util.SkyCoordinatesTranslator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SatelliteConfigurationServiceImpl implements SatelliteConfigurationService {
    private final SatelliteDBService satelliteDBService;

    private List<SatelliteMapDTO> satelliteList;

    @Override
    public List<SatelliteMapDTO> getSatelliteList(EarthPositionCoordinates position, ZonedDateTime dateTime) {
        if (satelliteList == null) {
            satelliteList = updateSatelliteList(position, dateTime);
        } else if (satelliteList.isEmpty()) {
            satelliteList = updateSatelliteList(position, dateTime);
        }
        return satelliteList;
    }

    @Override
    public List<SatelliteMapDTO> updateSatelliteList(EarthPositionCoordinates position, ZonedDateTime dateTime) {
        List<SatelliteMapDTO> satelliteCoordinates = new ArrayList<>();

        List<Satellite> satellites = satelliteDBService.getSatellitesFromDB();
        if (!satellites.isEmpty()) {
            for (Satellite satellite : satellites) {
                SkyEquatorialCoordinates equatorialCoordinates = SkyCoordinatesTranslator.calculateEquatorialCoordinates(
                        satellite, position, dateTime);
                SkyHorizontalCoordinates horizontalCoordinates = SkyCoordinatesTranslator.calculateSkyHorizontalCoordinates(
                        equatorialCoordinates, position, dateTime);
                SatelliteMapDTO satelliteMapDTO = new SatelliteMapDTO();
                satelliteMapDTO.setCoordinates(horizontalCoordinates);
                satelliteMapDTO.setObjectId(satellite.getNoradCatId());
                satelliteMapDTO.setObjectName(satellite.getObjectName());
                satelliteMapDTO.setVisible(satellite.getVisible());
                satelliteCoordinates.add(satelliteMapDTO);
            }
        } else log.info("Satellites DB empty");
        return satelliteCoordinates;
    }

    @Override
    public boolean saveSatelliteProperties(List<SatelliteMapDTO> satellites) {
        boolean result = true;
        for (SatelliteMapDTO satellite : satellites) {
            result = result &&
            satelliteDBService.setSatelliteVisible(satellite.getObjectId(), satellite.getVisible());
        }
        return result;
    }
}
