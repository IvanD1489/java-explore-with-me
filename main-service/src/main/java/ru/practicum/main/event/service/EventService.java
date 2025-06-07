package ru.practicum.main.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventFullDto> searchAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    EventFullDto createEvent(Long userId, NewEventDto dto);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request);

    List<EventShortDto> searchPublicEvents(String text, List<Long> categories, Boolean paid,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                           Boolean onlyAvailable, String sort, int from, int size, HttpServletRequest request);

    EventFullDto getPublicEvent(Long eventId, HttpServletRequest request);

    long getConfirmedRequests(Long eventId);

    long getEventViews(Long eventId);
}
