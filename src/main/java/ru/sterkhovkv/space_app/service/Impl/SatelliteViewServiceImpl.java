package ru.sterkhovkv.space_app.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;
import ru.sterkhovkv.space_app.dto.SatelliteViewDTO;
import ru.sterkhovkv.space_app.service.ObserverService;
import ru.sterkhovkv.space_app.service.SatelliteViewService;
import ru.sterkhovkv.space_app.service.SpaceObjectCoordinatesService;
import ru.sterkhovkv.space_app.service.SpaceObjectDataService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SatelliteViewServiceImpl implements SatelliteViewService {
    private final SpaceObjectCoordinatesService spaceObjectCoordinatesService;
    private final SpaceObjectDataService spaceObjectDataService;
    private final ObserverService observerService;

    @Override
    public String populateModel(Model model, boolean isSpaceStation) {
        SatelliteViewDTO dto = getSatellites(isSpaceStation);
        model.addAttribute("satelliteViewDTO", dto);
        return isSpaceStation ? "spacestations" : "satellites";
    }

    @Override
    public String handleSatelliteAction(Map<String, String> params, Model model, boolean isSpaceStation) {
        String action = params.get("action");

        if ("update".equals(action)) {
            SatelliteViewDTO updatedDto = updateSatellites(isSpaceStation);
            model.addAttribute("satelliteViewDTO", updatedDto);
        } else if ("save".equals(action)) {
            SatelliteViewDTO dto = saveSatellites(params, isSpaceStation);
            model.addAttribute("satelliteViewDTO", dto);
        }

        return isSpaceStation ? "spacestations" : "satellites";
    }

    private SatelliteViewDTO getSatellites(boolean isSpaceStation) {
        SatelliteViewDTO dto = new SatelliteViewDTO();
        List<SatelliteMapDTO> satelliteList = spaceObjectCoordinatesService.getSpaceObjectsList(
                observerService.getObserverPosition(),
                ZonedDateTime.now(ZoneId.of("UTC")),
                isSpaceStation
        );
        dto.setSatellites(satelliteList);
        return dto;
    }

    private SatelliteViewDTO updateSatellites(boolean isSpaceStation) {
        spaceObjectCoordinatesService.loadSpaceObjectsToCache(isSpaceStation);
        return getSatellites(isSpaceStation);
    }

    private SatelliteViewDTO saveSatellites(Map<String, String> params, boolean isSpaceStation) {
        SatelliteViewDTO dto = new SatelliteViewDTO();

        List<SatelliteMapDTO> satelliteList = spaceObjectCoordinatesService.getSpaceObjectsList(
                observerService.getObserverPosition(),
                ZonedDateTime.now(ZoneId.of("UTC")),
                isSpaceStation
        );

        for (int i = 0; i < satelliteList.size(); i++) {
            satelliteList.get(i).setVisible(params.containsKey("visible" + i) && params.get("visible" + i).equals("on"));
        }

        if (spaceObjectDataService.saveSpaceObjectsProperties(satelliteList)) {
            dto.setSuccessMessage("Успешно сохранено");
            spaceObjectCoordinatesService.invalidateCache(isSpaceStation);
        } else {
            dto.setErrorMessage("Ошибка сохранения");
        }
        dto.setSatellites(getSatellites(isSpaceStation).getSatellites());
        return dto;
    }
}
