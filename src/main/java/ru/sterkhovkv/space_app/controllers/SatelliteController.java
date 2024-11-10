package ru.sterkhovkv.space_app.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;
import ru.sterkhovkv.space_app.service.ObserverService;
import ru.sterkhovkv.space_app.service.SatelliteConfigurationService;
import ru.sterkhovkv.space_app.service.SpaceStationConfigurationService;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;


@Controller
@Slf4j
public class SatelliteController {
    private final SatelliteConfigurationService satelliteConfigurationService;
    private final SpaceStationConfigurationService spaceStationConfigurationService;
    private final ObserverService observerService;

    @Autowired
    public SatelliteController(SatelliteConfigurationService satelliteConfigurationService,
                               SpaceStationConfigurationService spaceStationConfigurationService,
                               ObserverService observerService) {
        this.satelliteConfigurationService = satelliteConfigurationService;
        this.spaceStationConfigurationService = spaceStationConfigurationService;
        this.observerService = observerService;
    }

    @GetMapping("/satellites")
    public String getSatellites(Model model) {
        model.addAttribute("satellites", satelliteConfigurationService.getSatelliteList(
                observerService.getObserverPosition(),
                ZonedDateTime.now(ZoneId.of("UTC"))
        ));
        return "satellites";
    }

    @PostMapping("/satellites")
    public String postSatellites(@RequestParam Map<String, String> params, Model model) {
        String action = params.get("action");
        if (action.equals("update")) {
            List<SatelliteMapDTO> satelliteList = satelliteConfigurationService.updateSatelliteList(
                    observerService.getObserverPosition(),
                    ZonedDateTime.now(ZoneId.of("UTC"))
            );
            model.addAttribute("satellites", satelliteList);
            return "satellites";
        } else if (action.equals("save")) {
            List<SatelliteMapDTO> satelliteList = satelliteConfigurationService.getSatelliteList(
                    observerService.getObserverPosition(),
                    ZonedDateTime.now(ZoneId.of("UTC"))
            );
            for (int i = 0; i < satelliteList.size(); i++) {
                if (!params.containsKey("visible" + i)) {
                    satelliteList.get(i).setVisible(false);
                } else if (params.get("visible" + i).equals("on")) {
                    satelliteList.get(i).setVisible(true);
                }
            }
            if (satelliteConfigurationService.saveSatelliteProperties(satelliteList)) {
                model.addAttribute("success", "Успешно сохранено");
            } else {
                model.addAttribute("error", "Ошибка сохранения");
            }
        }

        return "redirect:/satellites";
    }

    @GetMapping("/spacestations")
    public String getSpaceStations(Model model) {
        model.addAttribute("satellites", spaceStationConfigurationService.getSpaceStationsList(
                observerService.getObserverPosition(),
                ZonedDateTime.now(ZoneId.of("UTC"))
        ));
        return "spacestations";
    }

    @PostMapping("/spacestations")
    public String postSpaceStations(@RequestParam Map<String, String> params, Model model) {
        String action = params.get("action");
        if (action.equals("update")) {
            List<SatelliteMapDTO> spaceStationList = spaceStationConfigurationService.updateSpaceStationsList(
                    observerService.getObserverPosition(),
                    ZonedDateTime.now(ZoneId.of("UTC"))
            );
            model.addAttribute("satellites", spaceStationList);
            return "spacestations";
        } else if (action.equals("save")) {
            List<SatelliteMapDTO> spaceStationList = spaceStationConfigurationService.getSpaceStationsList(
                    observerService.getObserverPosition(),
                    ZonedDateTime.now(ZoneId.of("UTC"))
            );
            for (int i = 0; i < spaceStationList.size(); i++) {
                if (!params.containsKey("visible" + i)) {
                    spaceStationList.get(i).setVisible(false);
                } else if (params.get("visible" + i).equals("on")) {
                    spaceStationList.get(i).setVisible(true);
                }
            }
            if (spaceStationConfigurationService.saveSpaceStationProperties(spaceStationList)) {
                model.addAttribute("success", "Успешно сохранено");
            } else {
                model.addAttribute("error", "Ошибка сохранения");
            }
        }
        return "redirect:/spacestations";
    }
}
