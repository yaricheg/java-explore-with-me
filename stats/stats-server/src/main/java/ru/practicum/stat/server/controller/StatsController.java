package ru.practicum.stat.server.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stat.server.exception.BadRequestException;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ViewStatsDto;
import ru.practicum.stat.server.service.StatServiceImpl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class StatsController {

    private final StatServiceImpl statsServiceImpl;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void createHit(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        log.info("Создана новая запись в статистические данные по запросу");
        statsServiceImpl.addHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(@RequestParam("start") @NonNull String start,
                                                       @RequestParam("end") @NonNull String end,
                                                       @RequestParam(required = false, defaultValue = "") List<String> uris,
                                                       @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Сделан запрос на получение статистики по подключениям");
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        try {
            String decodedStart = URLDecoder.decode(start, StandardCharsets.UTF_8);
            String decodedEnd = URLDecoder.decode(end, StandardCharsets.UTF_8);
            startDateTime = LocalDateTime.parse(decodedStart, FORMATTER);
            endDateTime = LocalDateTime.parse(decodedEnd, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Неправильный формат даты: " + e.getMessage());
        }
        List<ViewStatsDto> results = statsServiceImpl.getViews(startDateTime, endDateTime, uris, unique);
        return ResponseEntity.ok(results);
    }
}
