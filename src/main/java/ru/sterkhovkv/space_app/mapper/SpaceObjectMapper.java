package ru.sterkhovkv.space_app.mapper;

import ru.sterkhovkv.space_app.dto.SatelliteDataDTO;
import ru.sterkhovkv.space_app.dto.SatelliteTLEDTO;
import ru.sterkhovkv.space_app.model.SpaceObject;

public interface SpaceObjectMapper {
    SpaceObject spaceObjectFromTLE(SatelliteTLEDTO satelliteTLEDTO);

    void setFieldsFromTLE(SpaceObject spaceObject, SatelliteTLEDTO satelliteTLEDTO);

    SpaceObject spaceObjectFromDto(SatelliteDataDTO satelliteDataDTO);

    void setFieldsFromDto(SpaceObject spaceObject, SatelliteDataDTO satelliteDataDTO);
}
