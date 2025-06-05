package ru.practicum.main.location.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.location.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/admin/locations")
@RequiredArgsConstructor
public class AdminLocationController {
    private final LocationService locationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationDto createLocation(@Valid @RequestBody LocationDto dto) {
        return locationService.createLocation(dto);
    }

    @GetMapping
    public List<LocationDto> getAllLocations() {
        return locationService.getAllLocations();
    }

    @GetMapping("/{locationId}")
    public LocationDto getLocation(@PathVariable Long locationId) {
        return locationService.getLocation(locationId);
    }

    @DeleteMapping("/{locationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable Long locationId) {
        locationService.deleteLocation(locationId);
    }
}
