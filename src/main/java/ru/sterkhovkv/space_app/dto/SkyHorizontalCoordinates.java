package ru.sterkhovkv.space_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkyHorizontalCoordinates {
    //Azimuth - Азимут
    //Decimal degrees
    private double az;

    //Altitude - Угол места
    //Decimal degrees
    private double alt;
}
