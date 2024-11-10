package ru.sterkhovkv.space_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkyEquatorialCoordinates {
    //Right Ascent - Прямое восхождение
    //Decimal hours
    private double ra;

    //Declination - Склонение
    //Decimal degrees
    private double dec;
}
