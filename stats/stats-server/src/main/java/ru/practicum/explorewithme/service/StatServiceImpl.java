package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.EndpointHitDto;
import ru.practicum.explorewithme.ViewStatsDto;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.model.EndpointHitMapper;
import ru.practicum.explorewithme.repository.EndpointRepository;
import ru.practicum.explorewithme.repository.ViewStatsInterface;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatsService {

    private final EndpointRepository endpointHitRepository;

    @Override
    public void addHit(EndpointHitDto hit) {
        endpointHitRepository.save(EndpointHitMapper.toEndpointHit(hit));
    }

    @Override
    public List<ViewStatsDto> getViews(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<String> processedUris = (uris != null) ? uris : List.of();

        if (start.isAfter(end)) {
            throw new BadRequestException("Start date must be before end date");
        }

        if (unique) {
            return processedUris.isEmpty()
                    ? getUniqueStats(start, end)
                    : getUniqueStatsByUris(start, end, processedUris);
        } else {
            return processedUris.isEmpty()
                    ? getAllStats(start, end)
                    : getStatsByUris(start, end, processedUris);
        }
    }

    private List<ViewStatsDto> getAllStats(LocalDateTime start, LocalDateTime end) {
        return convertProjections(endpointHitRepository.getAllViewStats(start, end));
    }

    private List<ViewStatsDto> getStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return convertProjections(endpointHitRepository.getViewStatsByUris(start, end, uris));
    }

    private List<ViewStatsDto> getUniqueStats(LocalDateTime start, LocalDateTime end) {
        return convertProjections(endpointHitRepository.getViewStatsUnique(start, end));
    }

    private List<ViewStatsDto> getUniqueStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return convertProjections(endpointHitRepository.getViewStatsByUrisUnique(start, end, uris));
    }

    private List<ViewStatsDto> convertProjections(List<ViewStatsInterface> projections) {
        return projections.stream()
                .map(proj -> ViewStatsDto.builder()
                        .app(proj.getApp())
                        .uri(proj.getUri())
                        .hits(proj.getHits())
                        .build())
                .toList();
    }
}