package ru.practicum.explorewithme.controller.events;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.*;
import ru.practicum.explorewithme.dto.participation.ParticipationRequestDto;
import ru.practicum.explorewithme.service.EventsService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventsController {

    @Autowired
    private final EventsService eventsService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsByIdPrivate(@PathVariable Long userId,
                                                    @RequestParam(defaultValue = "0", required = false) Integer from,
                                                    @RequestParam(defaultValue = "10", required = false) Integer size) {
        return eventsService.getEventsByUserId(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createNewEventByUser(@PathVariable Long userId,
                                             @RequestBody @Valid NewEventDto newEventDto) {
        return eventsService.createNewEventByUser(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventFullByUser(@PathVariable Long userId,
                                           @PathVariable Long eventId) {
        return eventsService.getEventFullByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto editEventByUser(@PathVariable Long userId,
                                        @PathVariable Long eventId,
                                        @RequestBody @Valid UpdateEventUserRequest request) {
        return eventsService.editEventByUser(userId, eventId, request);
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsEventByUser(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventsService.getRequestsEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateStatusRequestEventByUser(@PathVariable Long userId,
                                                                         @PathVariable Long eventId,
                                                                         @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        return eventsService.updateStatusRequestEventByUser(userId, eventId, updateRequest);
    }

}
