package ru.sterkhovkv.space_app.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.model.Observer;
import ru.sterkhovkv.space_app.repository.ObserverRepository;
import ru.sterkhovkv.space_app.service.ObserverService;
import ru.sterkhovkv.space_app.util.Constants;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class ObserverServiceImpl implements ObserverService {

    private ObserverRepository observerRepository;

    @Autowired
    public ObserverServiceImpl(ObserverRepository observerRepository) {
        this.observerRepository = observerRepository;
    }

    @Override
    public void setObserverPosition(EarthPositionCoordinates coordinates) {
        if (coordinates != null && coordinates.getLat() <= 90 && coordinates.getLat() >= -90
                && coordinates.getLon() <= 180 && coordinates.getLon() >= -180) {
            Observer observer = observerRepository.findFirstByName(Constants.OBSERVER_NAME);
            if (observer == null) {
                observer = new Observer();
                observer.setName(Constants.OBSERVER_NAME);
            }
            observer.setLatitude(coordinates.getLat());
            observer.setLongitude(coordinates.getLon());
            observer.setLastUpdated(ZonedDateTime.now(ZoneId.of("UTC")));
            observerRepository.save(observer);
        }
    }

    @Override
    public EarthPositionCoordinates getObserverPosition() {
        Observer observer = observerRepository.findFirstByName(Constants.OBSERVER_NAME);
        if (observer == null) {
            return null;
        } else {
            return new EarthPositionCoordinates(observer.getLatitude(), observer.getLongitude());
        }
    }
}
