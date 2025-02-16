package ru.sterkhovkv.space_app.service;

import org.springframework.ui.Model;

import java.util.Map;

public interface SatelliteViewService {
    String populateModel(Model model, boolean isSpaceStation);

    String handleSatelliteAction(Map<String, String> params, Model model, boolean isSpaceStation);
}
