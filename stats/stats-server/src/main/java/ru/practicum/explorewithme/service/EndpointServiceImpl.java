package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.EndpointHitDto;
import ru.practicum.explorewithme.ViewStatsDto;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.model.EndpointHit;
import ru.practicum.explorewithme.model.EndpointHitMapper;
import ru.practicum.explorewithme.repository.EndpointRepository;
import ru.practicum.explorewithme.repository.ViewStatsInterface;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EndpointServiceImpl implements EndpointService {

    private final EndpointRepository endpointRepository;

    @Override
    @Transactional
    public EndpointHitDto createHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = EndpointHitMapper.toEndpointHit(endpointHitDto);
        return EndpointHitMapper.toEndpointHitDto(endpointRepository.save(endpointHit));
    }

    @Override
    @Transactional
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<String> madeUris = (uris != null) ? uris : List.of();
        if (start.isAfter(end)) {
            throw new BadRequestException("Дата начала должна быть раньше даты конца");
        }

        if (unique == true) {
            return madeUris.isEmpty()
                    ? getUniqueStats(start, end)
                    : getUniqueStatsByUris(start, end, madeUris);
        } else {
            return madeUris.isEmpty()
                    ? getAllStats(start, end)
                    : getStatsByUris(start, end, madeUris);
        }
    }

    private List<ViewStatsDto> getAllStats(LocalDateTime start, LocalDateTime end) {
        return convertViewStatsInterface(endpointRepository.getAllViewStats(start, end));
    }

    private List<ViewStatsDto> getStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return convertViewStatsInterface(endpointRepository.getViewStatsByUris(start, end, uris));
    }

    private List<ViewStatsDto> getUniqueStats(LocalDateTime start, LocalDateTime end) {
        return convertViewStatsInterface(endpointRepository.getViewStatsUnique(start, end));
    }

    private List<ViewStatsDto> getUniqueStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return convertViewStatsInterface(endpointRepository.getViewStatsByUrisUnique(start, end, uris));
    }

    private List<ViewStatsDto> convertViewStatsInterface(List<ViewStatsInterface> viewStatsInterfaces) {
        return viewStatsInterfaces.stream()
                .map(viewStat -> ViewStatsDto.builder()
                        .app(viewStat.getApp())
                        .uri(viewStat.getUri())
                        .hits(viewStat.getHits())
                        .build())
                .toList();
    }
}
