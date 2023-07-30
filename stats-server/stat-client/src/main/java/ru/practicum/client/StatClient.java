package ru.practicum.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatDto;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class StatClient extends BaseClient {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public StatDto saveHit(HitDto hitDto) {
        Gson gson = new Gson();
        ResponseEntity<Object> responseEntity = post("/hit", hitDto);
        String json = gson.toJson(responseEntity.getBody());
        return gson.fromJson(json, StatDto.class);
    }

    public List<StatDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        Gson gson = new Gson();
        String urisString = String.join(",", uris);
        Map<String, Object> parameters = Map.of(
                "start", start.format(DATE_TIME_FORMAT),
                "end", end.format(DATE_TIME_FORMAT),
                "uris", urisString,
                "unique", unique
        );
        String path = "stats?start={start}&end={end}&uris={uris}&unique={unique}";
        ResponseEntity<Object> responseEntity = get(path, parameters);
        Object responseBody = responseEntity.getBody();

        // Deserialize the response into a list of StatDto objects
        Type listType = new TypeToken<List<StatDto>>(){}.getType();
        List<StatDto> statistics = gson.fromJson(gson.toJson(responseBody), listType);
        return statistics;
    }
}