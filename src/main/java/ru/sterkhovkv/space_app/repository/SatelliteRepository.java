package ru.sterkhovkv.space_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sterkhovkv.space_app.model.Satellite;

import java.time.ZonedDateTime;
import java.util.List;

public interface SatelliteRepository extends JpaRepository<Satellite, Integer> {
    Satellite findFirstByNoradCatId(int noradCatId);
    List<Satellite> findByEpochLessThan(ZonedDateTime dateTime);
    List<Satellite> findSatellitesByVisible(boolean visible);
    List<Satellite> findAllByOrderByIdAsc();
}
