package ru.sterkhovkv.space_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConstellationDTO {
    private String abbreviation;
    private int lineCount;
    private List<StarCatalogDTO> stars;
}
