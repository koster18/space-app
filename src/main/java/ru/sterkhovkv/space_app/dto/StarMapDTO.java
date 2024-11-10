package ru.sterkhovkv.space_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StarMapDTO {
    private long idCatalog;
    private SkyHorizontalCoordinates coordinates;
    private double vMag;
    private double bV;
}
