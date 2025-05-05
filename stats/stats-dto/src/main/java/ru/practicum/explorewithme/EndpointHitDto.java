package ru.practicum.explorewithme;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EndpointHitDto {

    private Long id;

    @NotBlank(message = "Поле 'app' обязательно для заполнения")
    private String app;

    @NotBlank(message = "Поле 'uri' обязательно для заполнения")
    private String uri;

    @NotBlank(message = "Поле 'ip' обязательно для заполнения")
    private String ip;

    @NotBlank(message = "Поле 'timestamp' обязательно для заполнения")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

}


