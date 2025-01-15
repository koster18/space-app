package ru.sterkhovkv.space_app.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.sterkhovkv.space_app.dto.EarthPositionCoordinates;
import ru.sterkhovkv.space_app.exception.WebRequestException;
import ru.sterkhovkv.space_app.service.GeocodeService;
import ru.sterkhovkv.space_app.service.external.WebClientService;


@Service
@RequiredArgsConstructor
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

    private final WebClientService webClientService;
    private final ObjectMapper objectMapper;

    @Override
    public EarthPositionCoordinates getCoordinates(String address) {
        String requestUrl = apiUrl + "?apikey=" + apiKey + "&geocode=" + address + extendedParams;
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
            JsonNode rootNode = objectMapper.readTree(response);

            JsonNode foundNode = rootNode.path("response")
                    .path("GeoObjectCollection")
                    .path("metaDataProperty")
                    .path("GeocoderResponseMetaData")
                    .path("found");
            int found = Integer.parseInt(foundNode.asText());
            if (found < 1) {
                throw new WebRequestException("Координаты не получены в ответе сервиса");
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
            throw new WebRequestException("Ошибка извлечения координат: " + e.getMessage());
        }
    }
}