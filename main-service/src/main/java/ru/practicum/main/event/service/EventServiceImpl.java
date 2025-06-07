package ru.practicum.main.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventState;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.event.repository.EventSpecifications;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.location.mapper.LocationMapper;
import ru.practicum.main.location.model.Location;
import ru.practicum.main.location.repository.LocationRepository;
import ru.practicum.main.request.model.RequestStatus;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final StatsClient statsClient;

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> searchAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("rangeEnd must not be before rangeStart");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        Specification<Event> spec = EventSpecifications.adminFilter(users, states, categories, rangeStart, rangeEnd);
        return eventRepository.findAll(spec, pageable).stream()
                .map(this::toFullDtoWithStats)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = {BadRequestException.class, ConflictException.class, NotFoundException.class})
    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Event date must be at least 2 hours from now");
            }
            if (event.getPublishedOn() != null && request.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConflictException("Event date must be at least 1 hour after publish date");
            }
            event.setEventDate(request.getEventDate());
        }
        if (request.getCategory() != null) {
            Category cat = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(cat);
        }
        if (request.getLocation() != null) {
            Location location = locationRepository.save(locationMapper.toLocation(request.getLocation()));
            event.setLocation(location);
        }
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Event must be in PENDING state to be published");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Cannot reject already published event");
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
        return toFullDtoWithStats(eventRepository.save(event));
    }

    @Transactional(rollbackFor = {NotFoundException.class})
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours from now");
        }
        Location location = locationRepository.save(locationMapper.toLocation(dto.getLocation()));
        Event event = eventMapper.toEvent(dto, user, category, location);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setPublishedOn(null);
        return toFullDtoWithStats(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return eventRepository.findByInitiatorId(userId, pageRequest)
                .stream()
                .map(this::toShortDtoWithStats)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        return toFullDtoWithStats(event);
    }

    @Transactional(rollbackFor = {ConflictException.class, NotFoundException.class})
    @Override
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Event date must be at least 2 hours from now");
            }
            event.setEventDate(request.getEventDate());
        }
        if (request.getCategory() != null) {
            Category cat = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(cat);
        }
        if (request.getLocation() != null) {
            Location location = locationRepository.save(locationMapper.toLocation(request.getLocation()));
            event.setLocation(location);
        }
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
        return toFullDtoWithStats(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> searchPublicEvents(String text, List<Long> categories, Boolean paid,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable, String sort, int from, int size, HttpServletRequest request) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("rangeEnd must not be before rangeStart");
        }
        Sort sortOrder = "VIEWS".equalsIgnoreCase(sort) ? Sort.by(Sort.Direction.DESC, "views") : Sort.by(Sort.Direction.ASC, "eventDate");
        Pageable pageable = PageRequest.of(from / size, size, sortOrder);
        Specification<Event> spec = EventSpecifications.publicFilter(text, categories, paid, rangeStart, rangeEnd);

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        return events.stream()
                .map(this::toShortDtoWithStats)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        return toFullDtoWithStats(event);
    }

    private EventFullDto toFullDtoWithStats(Event event) {
        long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        long views = getEventViews(event.getId());
        return eventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    private EventShortDto toShortDtoWithStats(Event event) {
        long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        long views = getEventViews(event.getId());
        return eventMapper.toEventShortDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public long getConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Override
    @Transactional(readOnly = true)
    public long getEventViews(Long eventId) {
        String uri = "/events/" + eventId;
        LocalDateTime start = LocalDateTime.of(0, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.now();
        ResponseEntity<List<StatsDto>> response = statsClient.getStats(start, end, List.of(uri), true);
        List<StatsDto> stats = response.getBody();
        if (stats != null && !stats.isEmpty()) {
            return stats.getFirst().getHits();
        } else {
            return 0L;
        }
    }
}
