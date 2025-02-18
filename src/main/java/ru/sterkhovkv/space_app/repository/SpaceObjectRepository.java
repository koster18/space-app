package ru.sterkhovkv.space_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sterkhovkv.space_app.model.SpaceObject;

import java.time.ZonedDateTime;
import java.util.List;

public interface SpaceObjectRepository extends JpaRepository<SpaceObject, Integer> {
    SpaceObject findFirstByNoradCatId(int noradCatId);
    SpaceObject findFirstByObjectName(String objectName);
    List<SpaceObject> findByEpochLessThan(ZonedDateTime dateTime);
    List<SpaceObject> findAllByVisibleAndSpaceStation(boolean visible, boolean spaceStation);
    List<SpaceObject> findAllBySpaceStationOrderByIdAsc(boolean spaceStation);
    List<SpaceObject> findAllByNoradCatIdIn(List<Integer> objectIds);
}
