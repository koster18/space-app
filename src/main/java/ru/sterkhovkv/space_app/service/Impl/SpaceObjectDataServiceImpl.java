package ru.sterkhovkv.space_app.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.scheduler.Schedulers;
import ru.sterkhovkv.space_app.dto.SatelliteDataDTO;
import ru.sterkhovkv.space_app.dto.SatelliteMapDTO;
import ru.sterkhovkv.space_app.mapper.SpaceObjectMapper;
import ru.sterkhovkv.space_app.model.SpaceObject;
import ru.sterkhovkv.space_app.repository.SpaceObjectRepository;
import ru.sterkhovkv.space_app.service.SatelliteService;
import ru.sterkhovkv.space_app.service.SpaceObjectDataService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
    public void saveSpaceObjectsToDB(boolean spaceStation) {
        AtomicInteger count = new AtomicInteger(0);
        var satelliteStream = spaceStation ? satelliteService.getSpaceStations() : satelliteService.getSatellites();

        satelliteStream
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(satelliteData -> saveSpaceObject(satelliteData, spaceStation, count))
                .doOnComplete(() -> {
                    log.info("Saved {} {}", count.get(), spaceStation ? "space stations" : "satellites");
                    clearOldSpaceObjects();
                })
                .subscribe();
    }

    private void saveSpaceObject(SatelliteDataDTO satelliteData, boolean spaceStation, AtomicInteger count) {
        SpaceObject spaceObject = repository.findFirstByNoradCatId(satelliteData.getNoradCatId());
        if (spaceObject == null) {
            spaceObject = spaceObjectMapper.spaceObjectFromDto(satelliteData);
        } else {
            spaceObjectMapper.setFieldsFromDto(spaceObject, satelliteData);
        }
        spaceObject.setSpaceStation(spaceStation);
        spaceObject.setVisible(true);
        repository.save(spaceObject);
        count.incrementAndGet();
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
