package ru.sterkhovkv.space_app.service;

import ru.sterkhovkv.space_app.model.SpaceStation;

import java.util.List;

public interface SpaceStationDBService {
    void saveSpaceStationsToDB();

    void clearOldSpaceStations();

    List<SpaceStation> getSpaceStationsFromDB();

    List<SpaceStation> getVisibleSpaceStationsFromDB();

    boolean setSpaceStationVisible(int ObjectId, boolean visible);
}
