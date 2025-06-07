package ru.practicum.main.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.location.model.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.location.id = :locationId")
    boolean existsByLocationId(@Param("locationId") Long locationId);

    @Query(value = "SELECT e.* FROM events e " +
            "JOIN locations l ON e.location_id = l.id " +
            "WHERE distance(:lat, :lon, l.lat, l.lon) <= l.radius", nativeQuery = true)
    List<Event> findEventsInLocation(@Param("lat") float lat, @Param("lon") float lon);

}