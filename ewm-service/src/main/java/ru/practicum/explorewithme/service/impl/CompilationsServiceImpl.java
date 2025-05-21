package ru.practicum.explorewithme.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.dto.compilation.UpdateCompilationRequest;
import ru.practicum.explorewithme.exceptions.NotFoundException;
import ru.practicum.explorewithme.mapper.CompilationMapper;
import ru.practicum.explorewithme.model.Compilation;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.repository.CompilationRepository;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.service.CompilationsService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompilationsServiceImpl implements CompilationsService {

    private final EventRepository eventRepository;

    private final CompilationRepository compilationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        if (pinned != null) {
            return compilationRepository.findByPinned(pinned).stream()
                    .skip(from)
                    .limit(size)
                    .map(CompilationMapper::toCompilationDto)
                    .toList();
        } else {
            return compilationRepository.findAll().stream()
                    .skip(from)
                    .limit(size)
                    .map(CompilationMapper::toCompilationDto)
                    .toList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationsById(Long compId) {
        return CompilationMapper.toCompilationDto(compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка событий с id " + compId + " не найдена")
        ));
    }

    @Override
    @Transactional
    public CompilationDto createNewCompilations(NewCompilationDto newCompilationDto) {
        List<Long> uniqueEvents = Optional.ofNullable(newCompilationDto.getEvents())
                .orElse(List.of())
                .stream()
                .distinct()
                .toList();
        List<Event> events = eventRepository.findByIdIn(uniqueEvents);
        if (uniqueEvents.size() != events.size()) {
            throw new NotFoundException("Одно или несколько событие(й) не найдено");
        }
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        compilation.setEvents(events);
        compilation.setPinned(Optional.ofNullable(newCompilationDto.getPinned()).orElse(false));
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilationsById(Long compId, UpdateCompilationRequest updateCompilationDto) {
        Compilation oldCompilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка событий с id " + compId + " не найдена")
        );
        if (updateCompilationDto.getPinned() != null
                && updateCompilationDto.getPinned() != oldCompilation.getPinned()) {
            oldCompilation.setPinned(updateCompilationDto.getPinned());
        }
        List<Long> uniqueEvents = Optional.ofNullable(updateCompilationDto.getEvents())
                .orElse(List.of())
                .stream()
                .distinct()
                .toList();
        List<Event> eventsList = eventRepository.findByIdIn(uniqueEvents);
        if (uniqueEvents.size() != eventsList.size()) {
            throw new NotFoundException("Одно или несколько событие(й) не найдено");
        }
        Set<Event> events = new HashSet<>(eventsList);
        Set<Event> oldEvents = new HashSet<>(oldCompilation.getEvents());

        if (!oldEvents.equals(events)) {
            oldCompilation.setEvents(eventsList);
        }
        if (updateCompilationDto.getTitle() != null
                && !oldCompilation.getTitle().equals(updateCompilationDto.getTitle())) {
            oldCompilation.setTitle(updateCompilationDto.getTitle());
        }
        return CompilationMapper.toCompilationDto(compilationRepository.save(oldCompilation));
    }

    @Override
    @Transactional
    public void deleteCompilationsById(Long compId) {
        try {
            compilationRepository.deleteById(compId);
        } catch (EmptyResultDataAccessException ex) {
            throw new NotFoundException("Подборка событий с id " + compId + " не найдена");
        }
    }
}
