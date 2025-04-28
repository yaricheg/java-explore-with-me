package ru.practicum.explorewithme;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;


@Component
@RequiredArgsConstructor
public class RestStatsClient {

    private final RestClient restClient = RestClient.create("${stats-server.url}");

    public ResponseEntity<List<ViewStatsDto>> getStats(LocalDateTime start,
                                                       LocalDateTime end, List<String> uris, Boolean unique) {
        var retrieve = restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/stats")
                            .queryParam("start", start)
                            .queryParam("end", end);
                    List<String> uris1 = uris;
                    if (uris1 != null && !uris1.isEmpty()) {
                        builder.queryParam("uris", uris1);
                    }
                    builder.queryParam("unique", unique);
                    return builder.build();
                })
                .retrieve();
        return (ResponseEntity<List<ViewStatsDto>>) retrieve;
    }


    public void createHit(EndpointHitDto endPointHitDto) {
        var retrieve = (ResponseEntity<Object>) restClient.post()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/hit");

                    return builder.build();
                })
                .body(endPointHitDto)
                .retrieve();
    }
}
