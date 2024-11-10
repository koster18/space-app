package ru.sterkhovkv.space_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StarCatalogDTO {
    private long idCatalog;
    private double vMag;
    private SkyEquatorialCoordinates coordinates;
    private double bV;
}
