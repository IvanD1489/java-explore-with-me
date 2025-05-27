package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.StatsDto;
import ru.practicum.stats.server.mapper.HitMapper;
import ru.practicum.stats.server.model.Hit;
import ru.practicum.stats.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public void saveHit(HitDto hitDto) {
        log.info("Saving hit: {}", hitDto);
        Hit hit = HitMapper.toHit(hitDto);
        statsRepository.save(hit);
        log.info("Hit saved successfully: {}", hit);
    }

    @Override
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Getting stats for start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        List<StatsDto> result;
        boolean urisProvided = uris != null && !uris.isEmpty();

        if (unique) {
            if (urisProvided) {
                result = statsRepository.getStatsUniqueIp(start, end, uris);
            } else {
                result = statsRepository.getStatsUniqueIpNoUris(start, end);
            }
        } else {
            if (urisProvided) {
                result = statsRepository.getStatsNonUniqueIp(start, end, uris);
            } else {
                result = statsRepository.getStatsNonUniqueIpNoUris(start, end);
            }
        }
        log.info("Stats retrieved: {}", result);
        return result;
    }
}
