package ru.practicum.main.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.location.mapper.LocationMapper;
import ru.practicum.main.location.model.Location;
import ru.practicum.main.location.repository.LocationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    public LocationDto createLocation(LocationDto dto) {
        Location location = locationMapper.toLocation(dto);
        Location saved = locationRepository.save(location);
        return locationMapper.toLocationDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public LocationDto getLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location not found"));
        return locationMapper.toLocationDto(location);
    }

    @Transactional(readOnly = true)
    @Override
    public List<LocationDto> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(locationMapper::toLocationDto)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = { NotFoundException.class })
    @Override
    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new NotFoundException("Location not found");
        }
        locationRepository.deleteById(id);
    }
}
