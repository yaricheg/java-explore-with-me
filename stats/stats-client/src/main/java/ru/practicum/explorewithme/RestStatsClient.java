package ru.practicum.explorewithme;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.explorewithme.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Component
public class RestStatsClient {
    private final RestClient restClient;

    @Autowired
    public RestStatsClient(@Value("${stats-server.url}") String address) {
        this.restClient = RestClient.create(address);
    }


    public ResponseEntity<List<ViewStatsDto>> getStats(LocalDateTime start,
                                                       LocalDateTime end, List<String> uris,
                                                       Boolean unique) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Дата начала должна быть раньше даты конца");
        }
        return restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/stats")
                            .queryParam("start", start)
                            .queryParam("end", end);
                    if (uris != null && !uris.isEmpty()) {
                        builder.queryParam("uris", uris);
                    }
                    builder.queryParam("unique", unique);
                    return builder.build();
                })
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, (request, response) -> {
                    log.info("Введен неправильный запрос");
                    throw new BadRequestException("Проверьте правильность запроса");
                })
                .body(ParameterizedTypeReference.forType(List.class));
    }


    public ResponseEntity<Void> createHit(@Valid EndpointHitDto endPointHitDto) {
        return restClient.post()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/hit");

                    return builder.build();
                })
                .body(endPointHitDto)
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, (request, response) -> {
                    log.info("Введен неправильный запрос");
                    throw new BadRequestException("Проверьте правильность запроса");
                })
                .toBodilessEntity();
    }
}
