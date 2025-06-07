package ru.practicum.main.location.service;

import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.location.model.Location;

import java.util.List;

public interface LocationService {
    LocationDto createLocation(LocationDto dto);

    LocationDto getLocation(Long id);

    List<LocationDto> getAllLocations();

    void deleteLocation(Long id);

    Location getLocationEntity(Long id);

    List<EventShortDto> getEventsByLocation(@PathVariable Long locationId);
}
