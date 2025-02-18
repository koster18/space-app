package ru.sterkhovkv.space_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RectangularCoordinates {
    // All arguments in km
    private double x;
    private double y;
    private double z;
}
