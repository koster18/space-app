package ru.sterkhovkv.space_app.service;

import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;

public interface ObserverService {
    void setObserverPosition(EarthPositionCoordinates coordinates);

    EarthPositionCoordinates getObserverPosition();

    Integer getTimeZone();

    void setTimeZone(Integer timeZone);
}
