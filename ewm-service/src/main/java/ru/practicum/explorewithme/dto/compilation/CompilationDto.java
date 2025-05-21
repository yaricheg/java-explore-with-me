package ru.practicum.explorewithme.dto.compilation;

import lombok.*;
import ru.practicum.explorewithme.dto.event.EventShortDto;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CompilationDto {

    private List<EventShortDto> events;

    private Long id;

    private Boolean pinned;

    private String title;

}
