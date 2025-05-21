package ru.practicum.explorewithme.service.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.participation.ParticipationRequestDto;
import ru.practicum.explorewithme.enums.EventState;
import ru.practicum.explorewithme.enums.RequestStatus;
import ru.practicum.explorewithme.exceptions.ConflictException;
import ru.practicum.explorewithme.exceptions.NotFoundException;
import ru.practicum.explorewithme.mapper.ParticipationMapper;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.Participation;
import ru.practicum.explorewithme.model.QEvent;
import ru.practicum.explorewithme.model.User;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.ParticipationRepository;
import ru.practicum.explorewithme.repository.UserRepository;
import ru.practicum.explorewithme.service.ParticipationService;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ParticipationServiceImpl implements ParticipationService {

    private final ParticipationRepository participationRepository;

    private final UserRepository userRepository;

    private final EventRepository eventRepository;

    private final JPAQueryFactory queryFactory;


    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsUser(Long userId) {
        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id "
                        + userId + "не найден")
        );
        return participationRepository.findByRequesterId(userId).stream()
                .map(ParticipationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        return participationRepository.findByEvent_Id(eventId).stream()
                .filter(participation -> Objects.equals(participation.getEvent().getInitiator().getId(), userId))
                .map(ParticipationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequestUser(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие с id "
                        + eventId + "не найдено")
        );

        User requester = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id "
                        + userId + "не найден")
        );

        List<Participation> participations = participationRepository
                .findByEvent_IdAndRequester_Id(eventId, userId);

        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Инициатор не может быть участником своего события");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Это событие не опубликовано");
        }

        if (!participations.isEmpty()) {
            throw new ConflictException("Запрос уже отправлен");
        }

        if (event.getParticipantLimit() != 0) {
            if (Objects.equals(event.getParticipantLimit(), event.getConfirmedRequests())) {
                throw new ConflictException("Достигнут лимит участников");
            }
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            QEvent qEvent = QEvent.event;
            queryFactory.update(qEvent)
                    .set(qEvent.confirmedRequests, qEvent.confirmedRequests.add(1))
                    .where(qEvent.id.eq(event.getId()))
                    .execute();

            return ParticipationMapper.toDto(participationRepository.save(Participation.builder()
                    .event(event)
                    .requester(requester)
                    .status(RequestStatus.CONFIRMED)
                    .build()));
        }
        Participation participation = Participation.builder()
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .build();
        return ParticipationMapper
                .toDto(participationRepository.save(participation));
    }


    @Override
    @Transactional
    public ParticipationRequestDto cancelRequestUser(Long userId, Long requestId) {
        Participation request = participationRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(
                () -> new NotFoundException("Запрос с id " + userId + "не найден")
        );
        request.setStatus(RequestStatus.CANCELED);
        return ParticipationMapper.toDto(participationRepository.save(request));
    }

   /* @Override
    @Transactional
    public EventRequestStatusUpdateResult setRequestsStatusResults(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new NotFoundException("Event with id=%d was not found".formatted(eventId))
        );

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Confirmation for this event isn't needed");
        }

        if (Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
            throw new ConflictException("The participant limit has been reached");
        }

        List<Participation> requests = repository.findByIdInAndEventId(updateRequest.getRequestIds(), eventId);

        for (Participation request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Only pending requests status can be changed");
            }
        }

        int confirmedCount = event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0;
        int participantLimit = event.getParticipantLimit();

        List<Participation> confirmedRequests = new ArrayList<>();
        List<Participation> rejectedRequests = new ArrayList<>();
        QEvent qEvent = QEvent.event;

        if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
            for (Participation request : requests) {
                if (confirmedCount >= participantLimit) {
                    throw new ConflictException("The participant limit has been reached");
                }

                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);

                queryFactory.update(qEvent)
                        .set(qEvent.confirmedRequests, qEvent.confirmedRequests.add(1))
                        .where(qEvent.id.eq(request.getEvent().getId()))
                        .execute();

                confirmedCount++;

                if (confirmedCount == participantLimit) {
                    List<Participation> pendingRequests = repository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);

                    for (Participation pendingRequest : pendingRequests) {
                        pendingRequest.setStatus(RequestStatus.REJECTED);
                        rejectedRequests.add(pendingRequest);
                    }

                    repository.saveAll(rejectedRequests);

                    break;
                }
            }

            repository.saveAll(confirmedRequests);

            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(confirmedRequests.stream().map(ParticipationMapper::toDto).toList())
                    .rejectedRequests(rejectedRequests.stream().map(ParticipationMapper::toDto).toList())
                    .build();
        } else if (updateRequest.getStatus() == RequestStatus.REJECTED) {
            for (Participation request : requests) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }

            repository.saveAll(rejectedRequests);

            return EventRequestStatusUpdateResult.builder()
                    .rejectedRequests(rejectedRequests.stream().map(ParticipationMapper::toDto).toList())
                    .build();
        } else {
            throw new ConflictException("Wrong update status");
        }
    }*/

}
