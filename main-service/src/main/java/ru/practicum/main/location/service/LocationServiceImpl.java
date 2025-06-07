package ru.practicum.main.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.service.EventService;
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
    private final EventMapper eventMapper;
    private final EventService eventService;

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

    @Override
    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new NotFoundException("Location not found");
        }
        if (locationRepository.existsByLocationId(id)) {
            throw new ConflictException("Location is used in events and cannot be deleted");
        }
        locationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Location getLocationEntity(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location not found"));
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getEventsByLocation(Long locationId) {
        Location location = getLocationEntity(locationId);
        List<Event> events = locationRepository.findEventsInLocation(location.getLat(), location.getLon());
        return events.stream()
                .map(event -> eventMapper.toEventShortDto(
                        event,
                        eventService.getConfirmedRequests(event.getId()),
                        eventService.getEventViews(event.getId())
                ))
                .collect(Collectors.toList());
    }
}
