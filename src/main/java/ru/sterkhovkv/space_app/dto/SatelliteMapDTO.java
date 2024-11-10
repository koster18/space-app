package ru.sterkhovkv.space_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SatelliteMapDTO {
    private String objectName;
    private int objectId;
    private SkyHorizontalCoordinates coordinates;
    Boolean visible;
}
