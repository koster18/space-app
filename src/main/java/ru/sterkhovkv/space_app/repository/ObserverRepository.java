package ru.sterkhovkv.space_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sterkhovkv.space_app.model.Observer;

public interface  ObserverRepository extends JpaRepository<Observer, Integer> {
    Observer findFirstByName(String observerName);
}
