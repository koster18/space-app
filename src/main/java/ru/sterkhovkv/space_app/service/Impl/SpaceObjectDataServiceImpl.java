package ru.sterkhovkv.space_app.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.sterkhovkv.space_app.dto.SatelliteDataDTO;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;
import ru.sterkhovkv.space_app.dto.SatelliteTLEDTO;
import ru.sterkhovkv.space_app.mapper.SpaceObjectMapper;
import ru.sterkhovkv.space_app.model.SpaceObject;
import ru.sterkhovkv.space_app.repository.SpaceObjectRepository;
import ru.sterkhovkv.space_app.service.SatelliteService;
import ru.sterkhovkv.space_app.service.SpaceObjectDataService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SpaceObjectDataServiceImpl implements SpaceObjectDataService {

    private final SpaceObjectRepository repository;
    private final SatelliteService satelliteService;
    private final SpaceObjectMapper spaceObjectMapper;

    @Override
    public Mono<Void> saveSpaceObjectsToDB(boolean spaceStation) {
        var satelliteStream = spaceStation ? satelliteService.getSpaceStations() : satelliteService.getSatellites();
        long startTime = System.currentTimeMillis();

        return satelliteStream
                .publishOn(Schedulers.boundedElastic())
                .collectList()
                .flatMap(satelliteDataList -> Mono.fromRunnable(() -> saveSpaceObjects(satelliteDataList, spaceStation))
                        .doOnSuccess(aVoid -> log.info("Saved {} {}, time wasted: {} ms",
                                satelliteDataList.size(), spaceStation ? "space stations" : "satellites",
                                (System.currentTimeMillis() - startTime))))
                .then(Mono.fromRunnable(this::clearOldSpaceObjects));
    }

    private void saveSpaceObjects(List<SatelliteDataDTO> satelliteDataList, boolean spaceStation) {
        List<SpaceObject> spaceObjectsToSave = new ArrayList<>();
        for (SatelliteDataDTO satelliteData : satelliteDataList) {
            SpaceObject spaceObject = repository.findFirstByNoradCatId(satelliteData.getNoradCatId());
            if (spaceObject == null) {
                spaceObject = spaceObjectMapper.spaceObjectFromDto(satelliteData);
            } else {
                spaceObjectMapper.setFieldsFromDto(spaceObject, satelliteData);
            }
            spaceObject.setSpaceStation(spaceStation);
            spaceObject.setVisible(true);
            spaceObjectsToSave.add(spaceObject);
        }

        repository.saveAll(spaceObjectsToSave);
    }

    @Override
    public Mono<Void> saveTLESpaceObjectsToDB(boolean spaceStation) {
        var satelliteStream = spaceStation ? satelliteService.getSpaceStationsTLE() : satelliteService.getSatellitesTLE();
        long startTime = System.currentTimeMillis();

        return satelliteStream
                .publishOn(Schedulers.boundedElastic())
                .collectList()
                .flatMap(satelliteDataList -> Mono.fromRunnable(() -> saveTLESpaceObjects(satelliteDataList, spaceStation))
                        .doOnSuccess(aVoid -> log.info("Saved TLE {} {}, time wasted: {} ms",
                                satelliteDataList.size(), spaceStation ? "space stations" : "satellites",
                                (System.currentTimeMillis() - startTime))))
                .then(Mono.fromRunnable(this::clearOldSpaceObjects));
    }

    private void saveTLESpaceObjects(List<SatelliteTLEDTO> satelliteDataList, boolean spaceStation) {
        List<SpaceObject> spaceObjectsToSave = new ArrayList<>();
        for (SatelliteTLEDTO satelliteData : satelliteDataList) {
            SpaceObject spaceObject = repository.findFirstByObjectName(satelliteData.getName());
            if (spaceObject == null) {
                spaceObject = spaceObjectMapper.spaceObjectFromTLE(satelliteData);
            } else {
                spaceObjectMapper.setFieldsFromTLE(spaceObject, satelliteData);
            }
            spaceObject.setSpaceStation(spaceStation);
            spaceObject.setVisible(true);
            spaceObjectsToSave.add(spaceObject);
        }

        repository.saveAll(spaceObjectsToSave);
    }

    @Override
    public void clearOldSpaceObjects() {
        List<SpaceObject> spaceObjects = repository.findByEpochLessThan(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(30));
        log.info("Clearing old objects: {}", spaceObjects.size());
        repository.deleteAll(spaceObjects);
    }

    @Override
    public List<SpaceObject> getSpaceObjectsFromDB(boolean spaceStation) {
        return repository.findAllBySpaceStationOrderByIdAsc(spaceStation);
    }

    @Override
    public List<SpaceObject> getVisibleSpaceObjectsFromDB(boolean spaceStation) {
        return repository.findAllByVisibleAndSpaceStation(true, spaceStation);
    }

    @Override
    public boolean saveSpaceObjectsProperties(List<SatelliteMapDTO> satellites) {
        List<Integer> objectIds = satellites.stream()
                .map(SatelliteMapDTO::getObjectId)
                .collect(Collectors.toList());
        List<SpaceObject> spaceObjects = repository.findAllByNoradCatIdIn(objectIds);

        Map<Integer, SpaceObject> spaceObjectMap = spaceObjects.stream()
                .collect(Collectors.toMap(SpaceObject::getNoradCatId, Function.identity()));

        for (SatelliteMapDTO satellite : satellites) {
            SpaceObject spaceObject = spaceObjectMap.get(satellite.getObjectId());
            if (spaceObject != null) {
                spaceObject.setVisible(satellite.getVisible());
            }
        }
        repository.saveAll(spaceObjects);

        return true;
    }
}
