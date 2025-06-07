package ru.practicum.main.location.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.location.model.Location;

import java.time.LocalDateTime;

@Component
public class LocationMapper {
    public LocationDto toLocationDto(Location location) {
        return LocationDto.builder()
                .id(location.getId())
                .name(location.getName())
                .description(location.getDescription())
                .lat(location.getLat())
                .lon(location.getLon())
                .radius(location.getRadius())
                .createdOn(location.getCreatedOn())
                .build();
    }

    public Location toLocation(LocationDto dto) {
        return Location.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .lat(dto.getLat())
                .lon(dto.getLon())
                .radius(dto.getRadius())
                .createdOn(LocalDateTime.now())
                .build();
    }
}

