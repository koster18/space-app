package ru.sterkhovkv.space_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SatelliteViewDTO {
    private List<SatelliteMapDTO> satellites;
    private String successMessage;
    private String errorMessage;
}
