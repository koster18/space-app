package ru.sterkhovkv.space_app.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sterkhovkv.space_app.service.SatelliteViewService;

import java.util.Map;


@Controller
@RequiredArgsConstructor
public class SatelliteController {
    private final SatelliteViewService satelliteViewService;

    @GetMapping("/satellites")
    public String getSatellites(Model model) {
        return satelliteViewService.populateModel(model, false);
    }

    @PostMapping("/satellites")
    public String postSatellites(@RequestParam Map<String, String> params, Model model) {
        return satelliteViewService.handleSatelliteAction(params, model, false);
    }

    @GetMapping("/spacestations")
    public String getSpaceStations(Model model) {
        return satelliteViewService.populateModel(model, true);
    }

    @PostMapping("/spacestations")
    public String postSpaceStations(@RequestParam Map<String, String> params, Model model) {
        return satelliteViewService.handleSatelliteAction(params, model, true);
    }
}
