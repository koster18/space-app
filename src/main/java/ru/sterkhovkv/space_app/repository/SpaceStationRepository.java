package ru.sterkhovkv.space_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sterkhovkv.space_app.model.SpaceStation;

import java.time.ZonedDateTime;
import java.util.List;

public interface SpaceStationRepository extends JpaRepository<SpaceStation, Integer> {
    SpaceStation findFirstByNoradCatId(int noradCatId);
    List<SpaceStation> findByEpochLessThan(ZonedDateTime dateTime);
    List<SpaceStation> findSatellitesByVisible(boolean visible);
    List<SpaceStation> findAllByOrderByIdAsc();
}
