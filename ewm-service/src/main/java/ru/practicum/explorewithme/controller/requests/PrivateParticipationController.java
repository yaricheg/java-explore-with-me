package ru.practicum.explorewithme.controller.requests;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.participation.ParticipationRequestDto;
import ru.practicum.explorewithme.service.ParticipationService;

import java.util.List;


@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateParticipationController {

    private final ParticipationService participationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsUser(@PathVariable Long userId) {
        return participationService.getRequestsUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequestUser(@PathVariable Long userId, @RequestParam Long eventId) {
        return participationService.createRequestUser(eventId, userId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequestUser(@PathVariable Long userId, @PathVariable Long requestId) {
        return participationService.cancelRequestUser(userId, requestId);
    }

}
