package ru.sterkhovkv.space_app.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;
import ru.sterkhovkv.space_app.service.ObserverService;
import ru.sterkhovkv.space_app.service.SpaceObjectCoordinatesService;
import ru.sterkhovkv.space_app.service.SpaceObjectDataService;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;


@Slf4j
@Controller
@RequiredArgsConstructor
public class SatelliteController {
    private final SpaceObjectCoordinatesService spaceObjectConfigurationService;
    private final SpaceObjectDataService spaceObjectDataService;
    private final ObserverService observerService;

    @GetMapping("/satellites")
    public String getSatellites(Model model) {
        model.addAttribute("satellites", spaceObjectConfigurationService.getSpaceObjectsList(
                observerService.getObserverPosition(),
                ZonedDateTime.now(ZoneId.of("UTC")),
                false
        ));
        return "satellites";
    }

    @PostMapping("/satellites")
    public String postSatellites(@RequestParam Map<String, String> params, Model model) {
        String action = params.get("action");
        if (action.equals("update")) {
            spaceObjectConfigurationService.loadSpaceObjectsToCache(false);
            List<SatelliteMapDTO> satelliteList = spaceObjectConfigurationService.getSpaceObjectsList(
                    observerService.getObserverPosition(),
                    ZonedDateTime.now(ZoneId.of("UTC")),
                    false
            );
            model.addAttribute("satellites", satelliteList);
            return "satellites";
        } else if (action.equals("save")) {
            List<SatelliteMapDTO> satelliteList = spaceObjectConfigurationService.getSpaceObjectsList(
                    observerService.getObserverPosition(),
                    ZonedDateTime.now(ZoneId.of("UTC")),
                    false
            );
            for (int i = 0; i < satelliteList.size(); i++) {
                if (!params.containsKey("visible" + i)) {
                    satelliteList.get(i).setVisible(false);
                } else if (params.get("visible" + i).equals("on")) {
                    satelliteList.get(i).setVisible(true);
                }
            }
            if (spaceObjectDataService.saveSpaceObjectsProperties(satelliteList)) {
                model.addAttribute("success", "Успешно сохранено");
            } else {
                model.addAttribute("error", "Ошибка сохранения");
            }
            model.addAttribute("satellites", satelliteList);
        }

        return "redirect:/satellites";
    }

    @GetMapping("/spacestations")
    public String getSpaceStations(Model model) {
        model.addAttribute("satellites", spaceObjectConfigurationService.getSpaceObjectsList(
                observerService.getObserverPosition(),
                ZonedDateTime.now(ZoneId.of("UTC")),
                true
        ));
        return "spacestations";
    }

    @PostMapping("/spacestations")
    public String postSpaceStations(@RequestParam Map<String, String> params, Model model) {
        String action = params.get("action");
        if (action.equals("update")) {
            spaceObjectConfigurationService.loadSpaceObjectsToCache(true);
            List<SatelliteMapDTO> spaceStationList = spaceObjectConfigurationService.getSpaceObjectsList(
                    observerService.getObserverPosition(),
                    ZonedDateTime.now(ZoneId.of("UTC")),
                    true
            );
            model.addAttribute("satellites", spaceStationList);
            return "spacestations";
        } else if (action.equals("save")) {
            List<SatelliteMapDTO> spaceStationList = spaceObjectConfigurationService.getSpaceObjectsList(
                    observerService.getObserverPosition(),
                    ZonedDateTime.now(ZoneId.of("UTC")),
                    true
            );
            for (int i = 0; i < spaceStationList.size(); i++) {
                if (!params.containsKey("visible" + i)) {
                    spaceStationList.get(i).setVisible(false);
                } else if (params.get("visible" + i).equals("on")) {
                    spaceStationList.get(i).setVisible(true);
                }
            }
            if (spaceObjectDataService.saveSpaceObjectsProperties(spaceStationList)) {
                model.addAttribute("success", "Успешно сохранено");
            } else {
                model.addAttribute("error", "Ошибка сохранения");
            }
            model.addAttribute("satellites", spaceStationList);
        }
        return "redirect:/spacestations";
    }
}
