package ru.sterkhovkv.space_app.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sterkhovkv.space_app.service.SkyMapViewService;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SkyMapController {
    private final SkyMapViewService skyMapViewService;

    @GetMapping("/skymap")
    public String getSkyMap(Model model) {
        skyMapViewService.handleGetSkyMap(model);
        return "skyMap";
    }

    @PostMapping("/skymap")
    public String updateSkyMap(@RequestParam Map<String, String> params, Model model) {
        skyMapViewService.handleUpdateSkyMapActions(params, model);
        return "skyMap";
    }
}