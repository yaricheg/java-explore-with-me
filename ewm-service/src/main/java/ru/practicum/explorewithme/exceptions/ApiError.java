package ru.practicum.explorewithme.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@NoArgsConstructor
@Builder
@Data
@AllArgsConstructor
public class ApiError {

    private HttpStatus status;

    private String reason;

    private String message;

    private LocalDateTime timestamp;

}

