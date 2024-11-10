package ru.sterkhovkv.space_app.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.service.ObserverService;
import ru.sterkhovkv.space_app.service.SatelliteDBService;
import ru.sterkhovkv.space_app.service.SpaceStationDBService;
import ru.sterkhovkv.space_app.util.SkyCoordinatesTranslator;
import ru.sterkhovkv.space_app.service.SkyMapService;
import ru.sterkhovkv.space_app.service.GeocodeService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class SkyMapController {
    private final GeocodeService geocodeService;
    private final ObserverService observerService;
    private final SkyMapService skyMapService;
    private final SatelliteDBService satelliteDBService;
    private final SpaceStationDBService spaceStationDBService;

    private final List<Integer> offsets;
    private Boolean drawStars;
    private Boolean drawConstellationLines;
    private Boolean drawSatellites;
    private Boolean drawSmallSatellites;

    @Autowired
    public SkyMapController(GeocodeService geocodeService,
                            ObserverService observerService,
                            SkyMapService skyMapService,
                            SatelliteDBService satelliteDBService,
                            SpaceStationDBService spaceStationDBService) {
        this.geocodeService = geocodeService;
        this.observerService = observerService;
        this.skyMapService = skyMapService;
        this.satelliteDBService = satelliteDBService;
        this.spaceStationDBService = spaceStationDBService;

        this.offsets = new ArrayList<>();
        for (int i = -12; i <= 12; i++) {
            offsets.add(i);
        }
        drawStars = true;
        drawConstellationLines = true;
        drawSatellites = true;
        drawSmallSatellites = false;
    }

    @GetMapping("/skymap")
    public String getSkyMap(Model model) {
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime nowLocal = ZonedDateTime.now();
        int zoneOffset = nowLocal.getOffset().getTotalSeconds() / 3600;

        String formattedDate = nowLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String formattedHours = nowLocal.format(DateTimeFormatter.ofPattern("HH"));
        String formattedMinutes = nowLocal.format(DateTimeFormatter.ofPattern("mm"));
        String formattedSeconds = nowLocal.format(DateTimeFormatter.ofPattern("ss"));
        return fillModel(model, nowUTC, zoneOffset, formattedDate, formattedHours, formattedMinutes, formattedSeconds);
    }


    @PostMapping("/skymap")
    public String updateSkyMap(@RequestParam Map<String, String> params, Model model) {
        String action = params.get("action");

        if ("getCoordinates".equals(action)) {
            String address = params.get("address");
            if (address != null && !address.isEmpty()) {
                geocodeService.getCoordinates(address)
                        .doOnNext(coordinates -> {
                            if (coordinates != null && coordinates.getLat() <= 90 && coordinates.getLat() >= -90
                                    && coordinates.getLon() <= 180 && coordinates.getLon() >= -180) {
                                observerService.setObserverPosition(coordinates);
                            } else {
                                model.addAttribute("error", "Некорректный адрес");
                            }
                        })
                        .doOnError(error -> {
                            model.addAttribute("error", "Не удалось получить координаты");
                        }).block();
            } else model.addAttribute("error", "Введите адрес");
        } else if ("updateSatellites".equals(action)) {
            spaceStationDBService.saveSpaceStationsToDB();
        } else if ("updateSmallSatellites".equals(action)) {
            satelliteDBService.saveSatellitesToDB();
        }
        drawStars = params.containsKey("showStars");
        drawConstellationLines = params.containsKey("showConstellationLines");
        drawSatellites = params.containsKey("showSatellites");
        drawSmallSatellites = params.containsKey("showSmallSatellites");

        String localDate = params.get("localDate");
        String localHours = params.get("localHours");
        String localMinutes = params.get("localMinutes");
        String localSeconds = params.get("localSeconds");
        if (localDate == null || localHours == null || localMinutes == null || localSeconds == null || params.get("timeZone") == null) {
            model.addAttribute("error", "Некоторые параметры отсутствуют");
        }
        localHours = String.format("%02d", Integer.parseInt(localHours));
        localMinutes = String.format("%02d", Integer.parseInt(localMinutes));
        localSeconds = String.format("%02d", Integer.parseInt(localSeconds));
        String dateTime = localDate + "T" + localHours + ":" + localMinutes + ":" + localSeconds;
        int timeZone = Integer.parseInt(params.get("timeZone"));
        ZonedDateTime nowLocal = LocalDateTime.parse(dateTime)
                .atOffset(ZoneOffset.ofHours(timeZone))
                .toZonedDateTime();
        ZonedDateTime nowUTC = nowLocal.withZoneSameInstant(ZoneId.of("UTC"));

        return fillModel(model, nowUTC, timeZone, localDate, localHours, localMinutes, localSeconds);
    }


    private String fillModel(Model model, ZonedDateTime nowUTC, int zoneOffset, String localDate,
                             String localHours, String localMinutes, String localSeconds) {
        EarthPositionCoordinates coordinates = observerService.getObserverPosition();
        String directionLat = coordinates.getLat() >= 0 ? "С.Ш." : "Ю.Ш.";
        String latitude = SkyCoordinatesTranslator.getString(coordinates.getLat(), directionLat);
        String directionLon = coordinates.getLon() >= 0 ? "В.Д." : "З.Д.";
        String longitude = SkyCoordinatesTranslator.getString(coordinates.getLon(), directionLon);

        model.addAttribute("latitude", latitude);
        model.addAttribute("longitude", longitude);
        model.addAttribute("currentDate", localDate);
        model.addAttribute("currentHours", localHours);
        model.addAttribute("currentMinutes", localMinutes);
        model.addAttribute("currentSeconds", localSeconds);
        model.addAttribute("zoneOffset", zoneOffset);
        model.addAttribute("offsets", offsets);
        model.addAttribute("showStars", drawStars);
        model.addAttribute("showConstellationLines", drawConstellationLines);
        model.addAttribute("showSatellites", drawSatellites);
        model.addAttribute("showSmallSatellites", drawSmallSatellites);
        model.addAttribute("skyMapImage", skyMapService.drawSkyMap(nowUTC,
                drawStars, drawConstellationLines, drawSatellites, drawSmallSatellites));

        return "skyMap";
    }
}
