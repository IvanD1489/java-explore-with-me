package ru.practicum.main.compilation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.model.Compilation;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.request.model.RequestStatus;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventMapper eventMapper;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    public CompilationDto toCompilationDto(Compilation compilation) {
        Set<EventShortDto> events = compilation.getEvents() == null ? Set.of() :
                compilation.getEvents().stream()
                        .map(event -> {
                            long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
                            long views = getEventViews(event.getId());
                            return eventMapper.toEventShortDto(event, confirmedRequests, views);
                        })
                        .collect(Collectors.toSet());
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(events)
                .build();
    }

    private long getEventViews(Long eventId) {
        String uri = "/events/" + eventId;
        LocalDateTime start = LocalDateTime.of(0, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.now();
        List<StatsDto> stats = statsClient.getStats(start, end, List.of(uri), false).getBody();
        if (stats != null && !stats.isEmpty()) {
            return stats.getFirst().getHits();
        }
        return 0L;
    }
}
