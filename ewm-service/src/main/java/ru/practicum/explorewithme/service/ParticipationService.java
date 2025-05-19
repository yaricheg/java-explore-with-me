package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.participation.ParticipationRequestDto;

import java.util.List;

public interface ParticipationService {
    List<ParticipationRequestDto> getRequestsUser(Long userId);

    ParticipationRequestDto createRequestUser(Long eventId, Long userId);

    ParticipationRequestDto cancelRequestUser(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequests(Long userId, Long eventId);
}
