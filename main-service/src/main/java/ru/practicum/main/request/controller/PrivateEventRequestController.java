package ru.practicum.main.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class PrivateEventRequestController {
    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult updateRequestStatuses(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest request) {
        return requestService.updateRequestStatuses(userId, eventId, request);
    }
}
