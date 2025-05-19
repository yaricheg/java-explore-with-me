package ru.practicum.explorewithme.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.ViewStatsDto;
import ru.practicum.explorewithme.dto.event.*;
import ru.practicum.explorewithme.dto.participation.ParticipationRequestDto;
import ru.practicum.explorewithme.enums.EventState;
import ru.practicum.explorewithme.enums.RequestStatus;
import ru.practicum.explorewithme.enums.SortRule;
import ru.practicum.explorewithme.exceptions.ConflictException;
import ru.practicum.explorewithme.exceptions.NotFoundException;
import ru.practicum.explorewithme.filter.EventFilter;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.mapper.ParticipationMapper;
import ru.practicum.explorewithme.model.*;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.ParticipationRepository;
import ru.practicum.explorewithme.repository.UserRepository;
import ru.practicum.explorewithme.service.EventsService;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventsServiceImpl implements EventsService {

    private final EventRepository eventRepository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final ParticipationRepository participationRepository;

    private final JPAQueryFactory queryFactory;

    private final EventFilter filter;

    private final StatsClient statsClient;


    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsAdmin(EventFilterDtoAdmin filterDto) {

        BooleanExpression predicate = filter.buildPredicate(filterDto);

        Pageable pageable = PageRequest.of(
                filterDto.getFrom() / filterDto.getSize(),
                filterDto.getSize()
        );

        Page<Event> page = eventRepository.findAll(predicate, pageable);

        Map<Long, Integer> eventsViews = getViewsForEvents(page.getContent());

        return page.getContent()
                .stream()
                .map(EventMapper::toEventFullDto)
                .map(dto -> {
                            dto.setViews(eventsViews.get(dto.getId()));
                            return dto;
                        }
                )
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto editEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event oldEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие с id "
                        + eventId + " не найдено")
        );

        if (request.getTitle() != null && !request.getTitle().equals(oldEvent.getTitle())) {
            oldEvent.setTitle(request.getTitle());
        }

        if (request.getAnnotation() != null && !request.getAnnotation().equals(oldEvent.getAnnotation())) {
            oldEvent.setAnnotation(request.getAnnotation());
        }

        if (request.getCategory() != null) {
            Category newCategory = categoryRepository.findById(request.getCategory()).orElseThrow(
                    () -> new NotFoundException("Категория с id " +
                            request.getCategory() + " не найдена")
            );

            if (!newCategory.equals(oldEvent.getCategory())) {
                oldEvent.setCategory(newCategory);
            }
        }

        if (request.getDescription() != null && !request.getDescription().equals(oldEvent.getDescription())) {
            oldEvent.setDescription(request.getDescription());
        }

        if (request.getEventDate() != null && !request.getEventDate().equals(oldEvent.getEventDate())) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Дата события должна быть не менее " +
                        "чем на час позже текущего времени");
            }
            oldEvent.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null && !request.getLocation().equals(oldEvent.getLocation())) {
            oldEvent.setLocation(request.getLocation());
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case REJECT_EVENT -> {
                    if (oldEvent.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Опубликованные события не могут быть отклонены.");
                    }
                    oldEvent.setState(EventState.CANCELED);
                }
                case PUBLISH_EVENT -> {
                    if (oldEvent.getState() != EventState.PENDING) {
                        throw new ConflictException("Невозможно опубликовать событие, так как оно " +
                                "не в правильном состоянии: " + oldEvent.getState());
                    }
                    oldEvent.setState(EventState.PUBLISHED);
                    oldEvent.setPublishedOn(LocalDateTime.now());
                }
            }
        }
        if (request.getPaid() != null && !request.getPaid().equals(oldEvent.getPaid())) {
            oldEvent.setPaid(request.getPaid());
        }

        if (request.getParticipantLimit() != null
                && !request.getParticipantLimit().equals(oldEvent.getParticipantLimit())) {
            oldEvent.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null
                && !request.getRequestModeration().equals(oldEvent.getRequestModeration())) {
            oldEvent.setRequestModeration(request.getRequestModeration());
        }

        return EventMapper.toEventFullDto(eventRepository.save(oldEvent));
    }

    @Override
    @Transactional
    public List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        Page<Event> page = eventRepository.findAllByInitiatorId(userId, pageable);

        Map<Long, Integer> eventsViews = getViewsForEvents(page.getContent());

        return page.getContent()
                .stream()
                .map(EventMapper::toEventShortDto)
                .map(dto -> {
                            dto.setViews(eventsViews.get(dto.getId()));
                            return dto;
                        }
                )
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto createNewEventByUser(Long userId, NewEventDto eventDto) {
        User initiator = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id=%d не найден"
                        .formatted(userId))
        );

        Category category = categoryRepository.findById(eventDto.getCategory()).orElseThrow(
                () -> new NotFoundException("Категория с id=%d не найдена"
                        .formatted(eventDto.getCategory()))
        );

        if (!eventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Поле eventDate должно содержать дату" +
                    ", которая еще не наступила");
        }
        Event event = EventMapper.toEvent(eventDto);
        event.setCategory(category);
        event.setInitiator(initiator);
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventFullByUser(Long userId, Long eventId) {
        Event publishedEvent = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new NotFoundException("Событие с id " + eventId + " не найдено")
        );
        EventFullDto eventFullDto = EventMapper.toEventFullDto(publishedEvent);
        eventFullDto.setViews(getViewsForEvents(List.of(publishedEvent)).get(eventFullDto.getId()));
        return eventFullDto;
    }

    @Override
    public EventFullDto editEventByUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event oldEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие с id " + eventId + " не найдено")
        );

        if (oldEvent.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Изменить можно только запланированные или отмененные события");
        }

        if (request.getTitle() != null && !request.getTitle().equals(oldEvent.getTitle())) {
            oldEvent.setTitle(request.getTitle());
        }

        if (request.getAnnotation() != null && !request.getAnnotation().equals(oldEvent.getAnnotation())) {
            oldEvent.setAnnotation(request.getAnnotation());
        }

        if (request.getCategory() != null) {
            Category newCategory = categoryRepository.findById(request.getCategory()).orElseThrow(
                    () -> new NotFoundException("Категория с id=%d не найдена"
                            .formatted(request.getCategory()))
            );

            if (!newCategory.equals(oldEvent.getCategory())) {
                oldEvent.setCategory(newCategory);
            }
        }

        if (request.getDescription() != null && !request.getDescription().equals(oldEvent.getDescription())) {
            oldEvent.setDescription(request.getDescription());
        }

        if (request.getEventDate() != null && !request.getEventDate().equals(oldEvent.getEventDate())) {
            if (!request.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException(("Field: eventDate. Error: " +
                        "должно содержать дату, которая еще не наступила. Value: %s")
                        .formatted(request.getEventDate().toString()));
            }

            oldEvent.setEventDate(request.getEventDate());
        }

        if (request.getLocation() != null && !request.getLocation().equals(oldEvent.getLocation())) {
            oldEvent.setLocation(request.getLocation());
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case CANCEL_REVIEW -> oldEvent.setState(EventState.CANCELED);
                case SEND_TO_REVIEW -> oldEvent.setState(EventState.PENDING);
            }
        }

        if (request.getPaid() != null && !request.getPaid().equals(oldEvent.getPaid())) {
            oldEvent.setPaid(request.getPaid());
        }

        if (request.getParticipantLimit() != null
                && !request.getParticipantLimit().equals(oldEvent.getParticipantLimit())) {
            oldEvent.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null
                && !request.getRequestModeration().equals(oldEvent.getRequestModeration())) {
            oldEvent.setRequestModeration(request.getRequestModeration());
        }
        return EventMapper.toEventFullDto(eventRepository.save(oldEvent));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsEventByUser(Long userId, Long eventId) {
        return participationRepository.findByEvent_Id(eventId).stream()
                .filter(participation -> Objects.equals(participation.getEvent()
                        .getInitiator().getId(), userId))
                .map(ParticipationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatusRequestEventByUser(Long userId,
                                                                         Long eventId,
                                                                         EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new NotFoundException("Event with id=%d was not found".formatted(eventId))
        );
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Confirmation for this event isn't needed");
        }
        if (Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
            throw new ConflictException("The participant limit has been reached");
        }
        List<Participation> requests = participationRepository.findByIdInAndEventId(updateRequest.getRequestIds(), eventId);
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
                    List<Participation> pendingRequests = participationRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);

                    for (Participation pendingRequest : pendingRequests) {
                        pendingRequest.setStatus(RequestStatus.REJECTED);
                        rejectedRequests.add(pendingRequest);
                    }

                    participationRepository.saveAll(rejectedRequests);

                    break;
                }
            }

            participationRepository.saveAll(confirmedRequests);

            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(confirmedRequests.stream().map(ParticipationMapper::toDto).toList())
                    .rejectedRequests(rejectedRequests.stream().map(ParticipationMapper::toDto).toList())
                    .build();
        } else if (updateRequest.getStatus() == RequestStatus.REJECTED) {
            for (Participation request : requests) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }

            participationRepository.saveAll(rejectedRequests);

            return EventRequestStatusUpdateResult.builder()
                    .rejectedRequests(rejectedRequests.stream().map(ParticipationMapper::toDto).toList())
                    .build();
        } else {
            throw new ConflictException("Wrong update status");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsPublic(EventFilterDto filterDto) {
        BooleanExpression predicate = filter.buildPredicate(filterDto);

        Pageable pageable = PageRequest.of(
                filterDto.getFrom() / filterDto.getSize(),
                filterDto.getSize(),
                createSort(filterDto.getSort())
        );

        Page<Event> page = eventRepository.findAll(predicate, pageable);

        Map<Long, Integer> eventsViews = getViewsForEvents(page.getContent());

        return page.getContent()
                .stream()
                .map(EventMapper::toEventShortDto)
                .map(dto -> {
                            dto.setViews(eventsViews.get(dto.getId()));
                            return dto;
                        }
                )
                .toList();

    }

    @Override
    @Transactional
    public EventFullDto getEventFullByIdPublic(Long eventId) {
        Event publishedEvent = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED).orElseThrow(
                () -> new NotFoundException("Event with id=%d was not found".formatted(eventId))
        );

        EventFullDto eventFullDto = EventMapper.toEventFullDto(publishedEvent);
        eventFullDto.setViews(getViewsForEvents(List.of(publishedEvent))
                .get(eventFullDto.getId()));
        return eventFullDto;
    }


    private Map<Long, Integer> getViewsForEvents(List<Event> events) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime earliestEvent = events.getFirst().getCreatedOn();
        for (Event event : events) {
            if (event.getCreatedOn().isBefore(earliestEvent)) {
                earliestEvent = event.getCreatedOn();
            }
        }
        String start = earliestEvent.format(formatter);
        String end = LocalDateTime.now().format(formatter);
        Map<String, Long> uriIdMap = new HashMap<>();
        List<String> eventsUris = events.stream()
                .map(event -> {
                    String uri = "/events/" + event.getId();

                    uriIdMap.put(uri, event.getId());

                    return uri;
                }).toList();
        ResponseEntity<Object> response =
                statsClient.getViewStats(start, end, eventsUris, true);
        Object responseBody = response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        List<ViewStatsDto> viewStatsList = mapper.convertValue(responseBody,
                new TypeReference<>() {
                });
        Map<Long, Integer> idsToViewsMap = new HashMap<>();
        if (viewStatsList.isEmpty()) {
            for (Event event : events) {
                idsToViewsMap.put(event.getId(), 0);
            }
        } else {
            for (ViewStatsDto viewStats : viewStatsList) {
                idsToViewsMap.put(uriIdMap.get(viewStats.getUri()), viewStats.getHits());
            }
        }
        return idsToViewsMap;
    }

    private Sort createSort(SortRule sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        return switch (sort) {
            case VIEWS -> Sort.by(Sort.Direction.DESC, "views");
            case EVENT_DATE -> Sort.by(Sort.Direction.DESC, "eventDate");
        };
    }

}
