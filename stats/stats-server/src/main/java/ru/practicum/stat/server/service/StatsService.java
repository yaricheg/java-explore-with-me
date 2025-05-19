package ru.practicum.stat.server.service;

import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void addHit(EndpointHitDto hit);

    List<ViewStatsDto> getViews(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}