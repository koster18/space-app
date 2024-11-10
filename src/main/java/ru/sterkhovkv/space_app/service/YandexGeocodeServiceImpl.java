package ru.sterkhovkv.space_app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;


@Service
@Slf4j
public class YandexGeocodeServiceImpl implements GeocodeService {
    @Value("${yandex.geocoder.api.key}")
    private String apiKey;
    @Value("${yandex.geocoder.api.baseUrl}")
    private String baseUrl;
    @Value("${yandex.geocoder.api.url}")
    private String apiUrl;
    @Value("${yandex.geocoder.api.extendedParams}")
    private String extendedParams;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public YandexGeocodeServiceImpl(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<EarthPositionCoordinates> getCoordinates(String address) {
        try {
            String requestUrl = apiUrl + "?apikey=" + apiKey + "&geocode=" + address + extendedParams;

            Mono<EarthPositionCoordinates> coordinates = webClient.get()
                    .uri(requestUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(this::extractCoordinates)
                    .onErrorResume(e -> {
                        log.error("Error in getCoordinates webClient: {}", e.getMessage());
                        return Mono.just(new EarthPositionCoordinates(200, 200)); // Возвращаем координаты по умолчанию
                    });

            return coordinates;
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private EarthPositionCoordinates extractCoordinates(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);

            JsonNode foundNode = rootNode.path("response")
                    .path("GeoObjectCollection")
                    .path("metaDataProperty")
                    .path("GeocoderResponseMetaData")
                    .path("found");
            int found = Integer.parseInt(foundNode.asText());
            if (found < 1) {
                return null;
            }

            JsonNode posNode = rootNode.path("response")
                    .path("GeoObjectCollection")
                    .path("featureMember")
                    .get(0)
                    .path("GeoObject")
                    .path("Point")
                    .path("pos");

            String pos = posNode.asText();
            String[] coordinates = pos.split(" ");

            double longitude = Double.parseDouble(coordinates[0]);
            double latitude = Double.parseDouble(coordinates[1]);

            return new EarthPositionCoordinates(latitude, longitude);
        } catch (Exception e) {
            log.error("Error in extractCoordinates: {}", e.getMessage());
            return new EarthPositionCoordinates(200, 200);
        }
    }
}