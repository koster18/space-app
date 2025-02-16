package ru.sterkhovkv.space_app.service;

import org.springframework.ui.Model;

import java.util.Map;

public interface SkyMapViewService {
    void handleGetSkyMap(Model model);

    void handleUpdateSkyMapActions(Map<String, String> params, Model model);
}
