package ru.sterkhovkv.space_app.service;

import reactor.core.publisher.Mono;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;

public interface GeocodeService {
    Mono<EarthPositionCoordinates> getCoordinates(String address);
}
