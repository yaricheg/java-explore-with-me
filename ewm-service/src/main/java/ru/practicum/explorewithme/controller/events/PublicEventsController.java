package ru.practicum.explorewithme.controller.events;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventFilterDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.service.EventsService;
import ru.practicum.stats.client.StatsClient;

import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class PublicEventsController {

    private final EventsService eventsService;

    private final StatsClient statsClient;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsPublic(@ModelAttribute EventFilterDto filterDto, HttpServletRequest request) {
        statsClient.postHit(request.getRequestURI(), request.getRemoteAddr());
        return eventsService.getEventsPublic(filterDto);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventFullByIdPublic(@PathVariable("eventId") Long eventId, HttpServletRequest request) {
        statsClient.postHit(request.getRequestURI(), request.getRemoteAddr());
        return eventsService.getEventFullByIdPublic(eventId);
    }

}
