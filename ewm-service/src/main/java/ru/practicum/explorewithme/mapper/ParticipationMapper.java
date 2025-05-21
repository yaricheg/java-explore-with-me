package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.participation.ParticipationRequestDto;
import ru.practicum.explorewithme.model.Participation;

public class ParticipationMapper {

    public static ParticipationRequestDto toDto(Participation participation) {
        return ParticipationRequestDto.builder()
                .id(participation.getId())
                .event(participation.getEvent().getId())
                .requester(participation.getRequester().getId())
                .status(participation.getStatus())
                .created(participation.getCreated())
                .build();
    }
}
