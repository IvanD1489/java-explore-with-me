package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.StatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsClient {

    private final RestTemplate restTemplate;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public StatsClient(String serverUrl) {
        this.restTemplate = new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public ResponseEntity<Void> addHit(HitDto hitDto) {
        HttpEntity<HitDto> requestEntity = new HttpEntity<>(hitDto);
        try {
            return restTemplate.exchange("/hit", HttpMethod.POST, requestEntity, Void.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    public ResponseEntity<List<StatsDto>> getStats(LocalDateTime start,
                                                   LocalDateTime end,
                                                   List<String> uris,
                                                   Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start.format(FORMATTER),
                "end", end.format(FORMATTER),
                "unique", unique
        );

        String path = "/stats?start={start}&end={end}&unique={unique}";

        if (uris != null && !uris.isEmpty()) {
            String urisParam = uris.stream().collect(Collectors.joining(","));
            path += "&uris=" + urisParam;
        }

        StringBuilder pathBuilder = new StringBuilder("/stats?start={start}&end={end}&unique={unique}");
        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                pathBuilder.append("&uris=").append(uri);
            }
        }

        Map<String, Object> effectiveParameters = new java.util.HashMap<>(parameters);
        if (uris != null && !uris.isEmpty()) {
            effectiveParameters.put("uris", uris.toArray(new String[0]));
        }


        try {
            String finalPath = "/stats?start={start}&end={end}&unique={unique}" +
                    ((uris != null && !uris.isEmpty()) ? "&uris={uris}" : "");


            ResponseEntity<List<StatsDto>> response = restTemplate.exchange(
                    finalPath,
                    HttpMethod.GET,
                    null,
                    new org.springframework.core.ParameterizedTypeReference<List<StatsDto>>() {
                    },
                    effectiveParameters
            );
            return response;
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
    }
}
