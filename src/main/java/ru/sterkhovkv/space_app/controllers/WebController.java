package ru.sterkhovkv.space_app.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.dto.SatelliteTLEDTO;
import ru.sterkhovkv.space_app.dto.SkyHorizontalCoordinates;
import ru.sterkhovkv.space_app.dto.SkyEquatorialCoordinates;
import ru.sterkhovkv.space_app.mapper.SpaceObjectMapper;
import ru.sterkhovkv.space_app.model.SpaceObject;
import ru.sterkhovkv.space_app.service.GeocodeService;
import ru.sterkhovkv.space_app.service.ObserverService;
import ru.sterkhovkv.space_app.service.SatelliteService;
import ru.sterkhovkv.space_app.sgp4.TLE;
import ru.sterkhovkv.space_app.util.SkyCoordinatesTranslator;
import ru.sterkhovkv.space_app.service.SkyMapService;
import ru.sterkhovkv.space_app.service.Impl.StarCatalogLoaderImpl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;


@Controller
@Slf4j
@RequiredArgsConstructor
public class WebController {
    private final GeocodeService geocodeService;
    private final ObserverService observerService;
    private final SatelliteService satelliteService;
    private final SpaceObjectMapper spaceObjectMapper;

    @GetMapping("/")
    public String index(Model model) {
        List<String> satelliteNames = satelliteService.getSatelliteNames().block();
        model.addAttribute("satelliteNames", satelliteNames);
        EarthPositionCoordinates coordinates = observerService.getObserverPosition();
        model.addAttribute("coordinates", coordinates);
        return "index";
    }

    @PostMapping("/getCoordinates")
    public Mono<String> getCoordinates(@RequestParam String address,
                                       Model model) {
        try {
            EarthPositionCoordinates coordinates = geocodeService.getCoordinates(address);
            observerService.setObserverPosition(coordinates);
            model.addAttribute("coordinates", coordinates);
            if ((coordinates.getLat() == 0) && coordinates.getLon() == 0) {
                model.addAttribute("error", "Получены некорректные координаты");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Не удалось получить координаты, ошибка: " + e.getMessage());
        }
        return Mono.just("index");


//
//        return geocodeService.getCoordinates(address)
//                .doOnNext(coordinates -> {
//                    observerService.setObserverPosition(coordinates);
//                    model.addAttribute("coordinates", coordinates);
////                    SkyHorizontalCoordinates skyCoordinates = calculatePolarStar(coordinates);
////                    model.addAttribute("skyCoordinates", skyCoordinates);
//                    if ((coordinates.getLat() == 0) && coordinates.getLon() == 0) {
//                        model.addAttribute("error", "Получены некорректные координаты");
////                    } else {
////                        // Добавляем azimuth и altitude в модель
////                        model.addAttribute("skyCoordinates.az", skyCoordinates.getAz());
////                        model.addAttribute("skyCoordinates.alt", skyCoordinates.getAlt());
//                    }
//                })
//                .doOnError(error -> {
//                    error.printStackTrace();
//                    model.addAttribute("error", "Не удалось получить координаты");
//                })
//                .then(Mono.just("index"));
    }

    @GetMapping("/calculate")
    public String showCalculationForm(Model model) {
        return "calculationForm";
    }

    @PostMapping("/calculate")
    public String calculateTimeForm(@RequestParam String longitude,
                                    Model model) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        double julianDay = SkyCoordinatesTranslator.calculateJulianDay(now);
        double siderealTimeDegrees = SkyCoordinatesTranslator.calculateUTCSiderealTime(now, Double.parseDouble(longitude));
        double siderealTimeHours = siderealTimeDegrees / 15;

        model.addAttribute("julianDay", julianDay);
        model.addAttribute("siderealTimeDegrees", siderealTimeDegrees);
        model.addAttribute("siderealTimeHours", siderealTimeHours);
        return "calculationForm";
    }

    private SkyHorizontalCoordinates calculatePolarStar(EarthPositionCoordinates coordinates) {
//        double ascent = 03.0 + 04.0 / 60 + 21.5 / 3600;
//        double declination = 89.0 + 21.0 / 60 + 50.3 / 3600;
        double ascent = 18 + 37.0 / 60 + 46.7 / 3600;
        double declination = 38 + 48.0 / 60 + 36.5 / 3600;

        SkyEquatorialCoordinates skyEquatorialCoordinates = new SkyEquatorialCoordinates();
        skyEquatorialCoordinates.setRa(ascent);
        skyEquatorialCoordinates.setDec(declination);

        ZonedDateTime timeUTC = ZonedDateTime.now(ZoneId.of("UTC"));

        return SkyCoordinatesTranslator.calculateSkyHorizontalCoordinates(
                skyEquatorialCoordinates, coordinates, timeUTC);
    }

//    @PostMapping("/selectSatellite")
//    public String selectSatellite(@RequestParam String satelliteName,
//                                  Model model) {
//        SatelliteDataDTO satelliteDataDTO = satelliteService.getSatelliteByName(satelliteName).block();
//        log.info("Satellite: {}", satelliteDataDTO);
//        if (satelliteDataDTO != null) {
//            ZonedDateTime nowUTC = ZonedDateTime.now(ZoneId.of("UTC"));
//            //LocalDateTime localDateTime = LocalDateTime.of(2024, 9, 20, 8, 9, 48);
//            //ZonedDateTime nowUTC = localDateTime.atZone(ZoneOffset.UTC);
//            SkyEquatorialCoordinates equatorialCoordinates = SkyCoordinatesTranslator.calculateEquatorialCoordinates(
//                    satelliteDataDTO, observerService.getObserverPosition(), nowUTC);
//            log.info("Equatorial: {}", equatorialCoordinates);
//            SkyHorizontalCoordinates horizontalCoordinates = SkyCoordinatesTranslator.calculateSkyHorizontalCoordinates(
//                    equatorialCoordinates, observerService.getObserverPosition(), nowUTC);
//            log.info("Horizontal: {}", horizontalCoordinates);
//            model.addAttribute("skyCoordinates.az", horizontalCoordinates.getAz());
//            model.addAttribute("skyCoordinates.alt", horizontalCoordinates.getAlt());
//        }
//        List<String> satelliteNames = satelliteService.getSatelliteNames().block();
//        model.addAttribute("satelliteNames", satelliteNames);
//        EarthPositionCoordinates coordinates = observerService.getObserverPosition();
//        model.addAttribute("coordinates", coordinates);
//
//        return "index";
//    }


    @GetMapping("/ISS")
    public String showISSForm(Model model) {
        SatelliteTLEDTO tleData = satelliteService.getTLEISS()
                .block().get(0);
        log.info("TLE Data[1]: {}", tleData.getLine1());
        log.info("TLE Data[2]: {}", tleData.getLine2());
        List<String> processedData = processTLEData(tleData);
//        for (List<Double> dataList : processedData) {
//            tleData.add(dataList.toString());
//        }

        //List<SatelliteTLEDTO> satelliteTLEDTOS = satelliteService.getSpaceStationsTLE().collectList().block();
        SpaceObject s = spaceObjectMapper.spaceObjectFromTLE(tleData);
        log.info("Satellite: {}", s.toString());

        model.addAttribute("tleList", List.of(tleData.getName(), tleData.getLine1(), tleData.getLine2()));
        return "ISSForm";
    }

    private List<String> processTLEData(SatelliteTLEDTO tleData) {
        long startTime = System.currentTimeMillis();
        TLE tle = new TLE(tleData.getLine1(), tleData.getLine1());
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneId.of("UTC"));
        double[][] rv = tle.getRV(Date.from(nowUTC.toInstant()));
        log.info("R {}", rv[0]);
        log.info("V {}", rv[1]);
        log.info("Время выполнения вычисления SGP4: {} мс", (System.currentTimeMillis() - startTime));
        return null;
//        return Arrays.stream(rv)
//                .map(arr -> Arrays.stream(arr).boxed().toList())
//                .toList();
    }
}
