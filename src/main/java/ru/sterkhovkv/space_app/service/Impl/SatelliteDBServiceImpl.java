package ru.sterkhovkv.space_app.service.Impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;
import ru.sterkhovkv.space_app.dto.SatelliteDataDTO;
import ru.sterkhovkv.space_app.model.Satellite;
import ru.sterkhovkv.space_app.repository.SatelliteRepository;
import ru.sterkhovkv.space_app.service.SatelliteDBService;
import ru.sterkhovkv.space_app.service.SatelliteService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SatelliteDBServiceImpl implements SatelliteDBService {
    private final SatelliteService satelliteService;
    private final SatelliteRepository satelliteRepository;


    @Override
    public void saveSatellitesToDB() {
        AtomicInteger count = new AtomicInteger(0);

        satelliteService.getSatellites()
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(satelliteData -> {
                    Satellite satellite = satelliteRepository.findFirstByNoradCatId(satelliteData.getNoradCatId());
                    if (satellite == null) {
                        satellite = new Satellite();
                        satellite.setVisible(true);
                    }
                    setSatelliteFields(satellite, satelliteData);
                    satelliteRepository.save(satellite);
                    count.incrementAndGet();
                })
                .doOnComplete(() -> {
                    log.info("Saved {} satellites", count.get());
                    clearOldSatellites();
                })
                .subscribe();
    }

    @Override
    public void clearOldSatellites() {
        List<Satellite> satellites = satelliteRepository.findByEpochLessThan(
                ZonedDateTime.now(ZoneId.of("UTC")).minusDays(30));
        log.info("Clearing old satellites: {}", satellites.size());
        satelliteRepository.deleteAll(satellites);
    }

    @Override
    public List<Satellite> getSatellitesFromDB() {
        return satelliteRepository.findAllByOrderByIdAsc();
    }

    @Override
    public List<Satellite> getVisibleSatellitesFromDB() {
        return satelliteRepository.findSatellitesByVisible(true);
    }

    @Override
    public boolean setSatelliteVisible(int ObjectId, boolean visible) {
        Satellite satellite = satelliteRepository.findFirstByNoradCatId(ObjectId);
        if (satellite == null) {
            return false;
        } else {
            satellite.setVisible(visible);
            satelliteRepository.save(satellite);
            return true;
        }
    }

    private static void setSatelliteFields(Satellite satellite, SatelliteDataDTO satelliteDataDTO) {
        satellite.setObjectName(satelliteDataDTO.getObjectName());
        satellite.setEpoch(satelliteDataDTO.getEpoch());
        satellite.setMeanMotion(satelliteDataDTO.getMeanMotion());
        satellite.setEccentricity(satelliteDataDTO.getEccentricity());
        satellite.setInclination(satelliteDataDTO.getInclination());
        satellite.setRaOfAscNode(satelliteDataDTO.getRaOfAscNode());
        satellite.setArgOfPericenter(satelliteDataDTO.getArgOfPericenter());
        satellite.setMeanAnomaly(satelliteDataDTO.getMeanAnomaly());
        satellite.setClassificationType(satelliteDataDTO.getClassificationType());
        satellite.setNoradCatId(satelliteDataDTO.getNoradCatId());
        satellite.setRevAtEpoch(satelliteDataDTO.getRevAtEpoch());
        satellite.setBstar(satelliteDataDTO.getBstar());
        satellite.setMeanMotionDot(satelliteDataDTO.getMeanMotionDot());
        satellite.setMeanMotionDdot(satelliteDataDTO.getMeanMotionDdot());
    }
}
