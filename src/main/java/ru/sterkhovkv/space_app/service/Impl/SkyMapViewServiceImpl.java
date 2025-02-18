package ru.sterkhovkv.space_app.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.enums.CalculationMode;
import ru.sterkhovkv.space_app.service.GeocodeService;
import ru.sterkhovkv.space_app.service.ObserverService;
import ru.sterkhovkv.space_app.service.SkyMapService;
import ru.sterkhovkv.space_app.service.SkyMapViewService;
import ru.sterkhovkv.space_app.service.SpaceObjectCoordinatesService;
import ru.sterkhovkv.space_app.service.SpaceObjectDataService;
import ru.sterkhovkv.space_app.util.SkyCoordinatesTranslator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class SkyMapViewServiceImpl implements SkyMapViewService {
    private final GeocodeService geocodeService;
    private final ObserverService observerService;
    private final SpaceObjectDataService spaceObjectDataService;
    private final SkyMapService skyMapService;
    private final SpaceObjectCoordinatesService spaceObjectCoordinatesService;

    private Boolean drawStars = true;
    private Boolean drawConstellationLines = true;
    private Boolean drawSatellites = true;
    private Boolean drawSmallSatellites = false;
    private CalculationMode calculationMode = CalculationMode.KEPLER_NEWTON;

    @Override
    public void handleGetSkyMap(Model model) {
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneId.of("UTC"));
        int zoneOffset = observerService.getTimeZone();
        ZoneOffset offset = ZoneOffset.ofHours(zoneOffset);
        ZonedDateTime nowLocal = nowUTC.withZoneSameInstant(offset);

        fillModel(model, nowUTC, zoneOffset, nowLocal);
    }

    @Override
    public void handleUpdateSkyMapActions(Map<String, String> params, Model model) {
        String action = params.get("action");

        switch (action) {
            case "getCoordinates":
                handleGetCoordinates(params.get("address"), model);
                break;
            case "updateSatellites":
                updateSatellites(true);
                break;
            case "updateSmallSatellites":
                updateSatellites(false);
                break;
            default:
                break;
        }

        updateDrawingPreferences(params);
        ZonedDateTime nowLocal = parseDateTime(params);
        ZonedDateTime nowUTC = nowLocal.withZoneSameInstant(ZoneId.of("UTC"));
        observerService.setTimeZone(Integer.parseInt(params.get("timeZone")));
        fillModel(model, nowUTC, Integer.parseInt(params.get("timeZone")), nowLocal);
    }

    @Override
    public String updateSkyMapImage(Map<String, String> params) {
        updateDrawingPreferences(params);
        ZonedDateTime nowLocal = parseDateTime(params);
        ZonedDateTime nowUTC = nowLocal.withZoneSameInstant(ZoneId.of("UTC"));

        return skyMapService.drawSkyMap(nowUTC, drawStars, drawConstellationLines, drawSatellites, drawSmallSatellites, calculationMode);
    }

    private void updateSatellites(boolean spaceStation) {
        spaceObjectDataService.saveTLESpaceObjectsToDB(spaceStation)
                .doOnSuccess(aVoid -> {
                    long startTime = System.currentTimeMillis();
                    spaceObjectCoordinatesService.invalidateCache(spaceStation);
                    spaceObjectCoordinatesService.loadSpaceObjectsToCache(spaceStation);
                    log.info("Time wasted for {} cache refresh: {} ms", spaceStation ? "space stations" : "satellites",
                            (System.currentTimeMillis() - startTime));
                })
                .subscribe();
    }


    public void handleGetCoordinates(String address, Model model) {
        if (address == null || address.trim().isEmpty()) {
            model.addAttribute("error", "Введите адрес");
            return;
        }

        try {
            EarthPositionCoordinates coordinates = geocodeService.getCoordinates(address);
            if (isValidCoordinates(coordinates)) {
                observerService.setObserverPosition(coordinates);
            } else {
                model.addAttribute("error", "Некорректный адрес");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Не удалось получить координаты. Ошибка: " + e.getMessage());
        }
    }

    public void updateDrawingPreferences(Map<String, String> params) {
        calculationMode = params.containsKey("calculationMode") ? CalculationMode.fromValue(params.get("calculationMode")) : CalculationMode.KEPLER_NEWTON;
        drawStars = params.containsKey("showStars");
        drawConstellationLines = params.containsKey("showConstellationLines");
        drawSatellites = params.containsKey("showSatellites");
        drawSmallSatellites = params.containsKey("showSmallSatellites");
    }

    public ZonedDateTime parseDateTime(Map<String, String> params) {
        String localDate = params.get("localDate");
        String localHours = params.get("localHours");
        String localMinutes = params.get("localMinutes");
        String localSeconds = params.get("localSeconds");
        String timeZoneParam = params.get("timeZone");
        if (localDate == null || localHours == null || localMinutes == null || localSeconds == null || timeZoneParam == null) {
            throw new IllegalArgumentException("Некоторые параметры отсутствуют");
        }
        String dateTime;
        int timeZone;
        try {
            dateTime = String.format("%sT%02d:%02d:%02d", localDate, Integer.parseInt(localHours),
                    Integer.parseInt(localMinutes),Integer.parseInt(localSeconds));
            timeZone = Integer.parseInt(timeZoneParam);
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректные параметры времени или даты");
        }
        return LocalDateTime.parse(dateTime)
                .atOffset(ZoneOffset.ofHours(timeZone))
                .toZonedDateTime();
    }

    public void fillModel(Model model, ZonedDateTime nowUTC, int zoneOffset, ZonedDateTime nowLocal) {
        EarthPositionCoordinates coordinates = Optional.ofNullable(observerService.getObserverPosition())
                .orElse(new EarthPositionCoordinates(0, 0));

        model.addAttribute("latitude", SkyCoordinatesTranslator.getString(coordinates.getLat(), coordinates.getLat() >= 0 ? "С.Ш." : "Ю.Ш."));
        model.addAttribute("longitude", SkyCoordinatesTranslator.getString(coordinates.getLon(), coordinates.getLon() >= 0 ? "В.Д." : "З.Д."));
        model.addAttribute("currentDate", nowLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("currentHours", formatTime(nowLocal, "HH"));
        model.addAttribute("currentMinutes", formatTime(nowLocal, "mm"));
        model.addAttribute("currentSeconds", formatTime(nowLocal, "ss"));
        model.addAttribute("zoneOffset", zoneOffset);
        model.addAttribute("offsets", IntStream.rangeClosed(-12, 12).boxed().collect(Collectors.toList()));
        model.addAttribute("calculationMode", calculationMode.getValue());
        model.addAttribute("showStars", drawStars);
        model.addAttribute("showConstellationLines", drawConstellationLines);
        model.addAttribute("showSatellites", drawSatellites);
        model.addAttribute("showSmallSatellites", drawSmallSatellites);
        model.addAttribute("skyMapImage", skyMapService.drawSkyMap(nowUTC,
                drawStars, drawConstellationLines, drawSatellites, drawSmallSatellites, calculationMode));
    }

    private String formatTime(ZonedDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public boolean isValidCoordinates(EarthPositionCoordinates coordinates) {
        return coordinates != null &&
                coordinates.getLat() >= -90 && coordinates.getLat() <= 90 &&
                coordinates.getLon() >= -180 && coordinates.getLon() <= 180;
    }
}
