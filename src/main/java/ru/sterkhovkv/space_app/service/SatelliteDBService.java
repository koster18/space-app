package ru.sterkhovkv.space_app.service;

import ru.sterkhovkv.space_app.model.Satellite;

import java.util.List;

public interface SatelliteDBService {


    void saveSatellitesToDB();

    void clearOldSatellites();

    List<Satellite> getSatellitesFromDB();

    List<Satellite> getVisibleSatellitesFromDB();

    boolean setSatelliteVisible(int ObjectId, boolean visible);
}
