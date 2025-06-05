package ru.practicum.main.location.service;

import ru.practicum.main.location.dto.LocationDto;

import java.util.List;

public interface LocationService {
    LocationDto createLocation(LocationDto dto);

    LocationDto getLocation(Long id);

    List<LocationDto> getAllLocations();

    void deleteLocation(Long id);
}
