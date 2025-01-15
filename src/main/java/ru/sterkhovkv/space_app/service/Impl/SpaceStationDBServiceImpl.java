package ru.sterkhovkv.space_app.service.Impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;
import ru.sterkhovkv.space_app.dto.SatelliteDataDTO;
import ru.sterkhovkv.space_app.model.SpaceStation;
import ru.sterkhovkv.space_app.repository.SpaceStationRepository;
import ru.sterkhovkv.space_app.service.SatelliteService;
import ru.sterkhovkv.space_app.service.SpaceStationDBService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SpaceStationDBServiceImpl implements SpaceStationDBService {
    private final SpaceStationRepository spaceStationRepository;
    private final SatelliteService satelliteService;

    @Override
    public void saveSpaceStationsToDB() {
        AtomicInteger count = new AtomicInteger(0);

        satelliteService.getSpaceStations()
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(satelliteData -> {
                    SpaceStation spaceStation = spaceStationRepository.findFirstByNoradCatId(satelliteData.getNoradCatId());
                    if (spaceStation == null) {
                        spaceStation = new SpaceStation();
                        spaceStation.setVisible(true);
                    }
                    setSpaceStationsFields(spaceStation, satelliteData);
                    spaceStationRepository.save(spaceStation);
                    count.incrementAndGet();
                })
                .doOnComplete(() -> {
                    log.info("Saved {} space stations", count.get());
                    clearOldSpaceStations();
                })
                .subscribe();
    }

    @Override
    public void clearOldSpaceStations() {
        List<SpaceStation> spaceStations = spaceStationRepository.findByEpochLessThan(
                ZonedDateTime.now(ZoneId.of("UTC")).minusDays(30));
        log.info("Clearing old satellites: {}", spaceStations.size());
        spaceStationRepository.deleteAll(spaceStations);
    }

    @Override
    public List<SpaceStation> getSpaceStationsFromDB() {
        return spaceStationRepository.findAllByOrderByIdAsc();
    }

    @Override
    public List<SpaceStation> getVisibleSpaceStationsFromDB() {
        return spaceStationRepository.findSatellitesByVisible(true);
    }

    @Override
    public boolean setSpaceStationVisible(int ObjectId, boolean visible) {
        SpaceStation spaceStation = spaceStationRepository.findFirstByNoradCatId(ObjectId);
        if (spaceStation == null) {
            return false;
        } else {
            spaceStation.setVisible(visible);
            spaceStationRepository.save(spaceStation);
            return true;
        }
    }

    private static void setSpaceStationsFields(SpaceStation spaceStation, SatelliteDataDTO satelliteDataDTO) {
        spaceStation.setObjectName(satelliteDataDTO.getObjectName());
        spaceStation.setEpoch(satelliteDataDTO.getEpoch());
        spaceStation.setMeanMotion(satelliteDataDTO.getMeanMotion());
        spaceStation.setEccentricity(satelliteDataDTO.getEccentricity());
        spaceStation.setInclination(satelliteDataDTO.getInclination());
        spaceStation.setRaOfAscNode(satelliteDataDTO.getRaOfAscNode());
        spaceStation.setArgOfPericenter(satelliteDataDTO.getArgOfPericenter());
        spaceStation.setMeanAnomaly(satelliteDataDTO.getMeanAnomaly());
        spaceStation.setClassificationType(satelliteDataDTO.getClassificationType());
        spaceStation.setNoradCatId(satelliteDataDTO.getNoradCatId());
        spaceStation.setRevAtEpoch(satelliteDataDTO.getRevAtEpoch());
        spaceStation.setBstar(satelliteDataDTO.getBstar());
        spaceStation.setMeanMotionDot(satelliteDataDTO.getMeanMotionDot());
        spaceStation.setMeanMotionDdot(satelliteDataDTO.getMeanMotionDdot());
    }

}
