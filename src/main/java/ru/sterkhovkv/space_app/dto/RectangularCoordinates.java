package ru.sterkhovkv.space_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RectangularCoordinates {
    // All arguments in km
    private double x;
    private double y;
    private double z;
}
