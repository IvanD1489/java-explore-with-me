package ru.practicum.main.location.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.location.model.Location;

@Component
public class LocationMapper {
    public LocationDto toLocationDto(Location location) {
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    public Location toLocation(LocationDto dto) {
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }
}
