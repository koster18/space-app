package ru.sterkhovkv.space_app.service;

import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;

public interface GeocodeService {
    EarthPositionCoordinates getCoordinates(String address);
}
