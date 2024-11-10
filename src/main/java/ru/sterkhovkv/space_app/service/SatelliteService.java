package ru.sterkhovkv.space_app.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.sterkhovkv.space_app.dto.SatelliteDataDTO;
import ru.sterkhovkv.space_app.dto.SatelliteTLEDTO;

import java.util.List;

public interface SatelliteService {
    Flux<SatelliteDataDTO> getSpaceStations();
    Mono<List<String>> getSpaceStationsNames();

    Flux<SatelliteDataDTO> getSatellites();
    Mono<List<String>> getSatelliteNames();

    Flux<SatelliteTLEDTO> getSpaceStationsTLE();
    Flux<SatelliteTLEDTO> getSatellitesTLE();

    Mono<List<SatelliteTLEDTO>> getTLEISS();
}
