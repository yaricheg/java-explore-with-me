package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.event.*;
import ru.practicum.explorewithme.dto.participation.ParticipationRequestDto;

import java.util.List;

public interface EventsService {

    List<EventFullDto> getEventsAdmin(EventFilterDtoAdmin filterDtoAdmin);

    EventFullDto editEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size);

    EventFullDto createNewEventByUser(Long userId, NewEventDto newEventDto);

    EventFullDto getEventFullByUser(Long userId, Long eventId);

    EventFullDto editEventByUser(Long userId, Long eventId, UpdateEventUserRequest request);

    List<ParticipationRequestDto> getRequestsEventByUser(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatusRequestEventByUser(Long userId, Long eventId,
                                                                  EventRequestStatusUpdateRequest updateRequest);

    List<EventShortDto> getEventsPublic(EventFilterDto filterDto);

    EventFullDto getEventFullByIdPublic(Long eventId);

}
