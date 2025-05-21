package ru.practicum.explorewithme.controller.compilations;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.dto.compilation.UpdateCompilationRequest;
import ru.practicum.explorewithme.service.CompilationsService;

@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationsController {

    @Autowired
    private final CompilationsService compilationsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createNewCompilations(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        return compilationsService.createNewCompilations(newCompilationDto);
    }

    @PatchMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilationsById(@PathVariable Long compId,
                                                 @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest) {
        return compilationsService.updateCompilationsById(compId, updateCompilationRequest);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilationsById(@PathVariable Long compId) {
        compilationsService.deleteCompilationsById(compId);
    }
}
