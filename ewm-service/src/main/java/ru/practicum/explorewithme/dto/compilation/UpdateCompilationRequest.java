package ru.practicum.explorewithme.dto.compilation;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Builder
@Data
@AllArgsConstructor
public class UpdateCompilationRequest {

    private List<Long> events;

    @Builder.Default
    private Boolean pinned = false;

    @Size(min = 1, max = 50)
    private String title;

}
