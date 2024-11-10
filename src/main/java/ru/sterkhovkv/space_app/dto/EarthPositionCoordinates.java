package ru.sterkhovkv.space_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EarthPositionCoordinates {
    //Latitude - Широта
    //Decimal degrees
    private double lat;

    //Longitude - Долгота
    //Decimal degrees
    private double lon;

    //Altitude - Высота
    //Meters
    //private double alt = 0;
}
