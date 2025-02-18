package ru.sterkhovkv.space_app.service;

import reactor.core.publisher.Mono;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;
import ru.sterkhovkv.space_app.model.SpaceObject;

import java.util.List;

public interface SpaceObjectDataService {
    Mono<Void> saveSpaceObjectsToDB(boolean spaceStation);
    Mono<Void> saveTLESpaceObjectsToDB(boolean spaceStation);
    void clearOldSpaceObjects();
    List<SpaceObject> getSpaceObjectsFromDB(boolean spaceStation);
    List<SpaceObject> getVisibleSpaceObjectsFromDB(boolean spaceStation);
    boolean saveSpaceObjectsProperties(List<SatelliteMapDTO> satellites);
}
