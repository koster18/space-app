package ru.sterkhovkv.space_app.service;

import java.time.ZonedDateTime;

public interface SkyMapService {
    String drawSkyMap(ZonedDateTime dateTime,
                      Boolean drawStars,
                      Boolean drawConstellationLines,
                      Boolean drawSatellites,
                      Boolean showSmallSatellites);
}
