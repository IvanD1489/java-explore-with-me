package ru.practicum.stats.server.mapper;

import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.server.model.Hit;

public class HitMapper {

    public static Hit toHit(HitDto dto) {
        return Hit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public static HitDto toHitDto(Hit entity) {
        return HitDto.builder()
                .app(entity.getApp())
                .uri(entity.getUri())
                .ip(entity.getIp())
                .timestamp(entity.getTimestamp())
                .build();
    }

}
