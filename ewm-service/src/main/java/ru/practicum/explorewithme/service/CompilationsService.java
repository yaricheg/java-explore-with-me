package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationsService {

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationsById(Long compId);

    CompilationDto createNewCompilations(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilationsById(Long compId, UpdateCompilationRequest updateCompilationRequest);

    void deleteCompilationsById(Long compId);
}
