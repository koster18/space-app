package ru.sterkhovkv.space_app.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.exception.WebRequestException;
import ru.sterkhovkv.space_app.service.GeocodeService;
import ru.sterkhovkv.space_app.service.external.WebClientService;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class OpenStreetMapGeocodeServiceImpl implements GeocodeService {
    @Value("${openstreetmap.geocoder.api.baseUrl}")
    private String baseUrl;
    @Value("${openstreetmap.geocoder.api.url}")
    private String apiUrl;
    @Value("${openstreetmap.geocoder.api.extendedParams}")
    private String extendedParams;

    private final WebClientService webClientService;
    private final ObjectMapper objectMapper;

    @Override
    public EarthPositionCoordinates getCoordinates(String address) {
        String requestUrl = apiUrl + address + extendedParams;

        try {
            return webClientService.get(requestUrl, String.class)
                    .map(this::extractCoordinates)
                    .onErrorResume(e -> {
                        log.error("Error in getCoordinates webClientService: {}", e.getMessage());
                        throw new WebRequestException("Ошибка при выполнении запроса: " + e.getMessage());
                    })
                    .block();
        } catch (Exception e) {
            log.error("Error in getCoordinates: {}", e.getMessage());
            throw new WebRequestException("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    private EarthPositionCoordinates extractCoordinates(String response) {
        try {
            JsonNode[] results = objectMapper.readValue(response, JsonNode[].class);
            if (results.length == 0) {
                throw new WebRequestException("Координаты не получены в ответе сервиса");
            }

            JsonNode firstResult = results[0];
            double latitude = firstResult.path("lat").asDouble();
            double longitude = firstResult.path("lon").asDouble();

            return new EarthPositionCoordinates(latitude, longitude);
        } catch (Exception e) {
            log.error("Error in extractCoordinates: {}", e.getMessage());
            throw new WebRequestException("Ошибка извлечения координат: " + e.getMessage());
        }
    }
}

