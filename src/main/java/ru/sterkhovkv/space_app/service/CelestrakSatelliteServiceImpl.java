package ru.sterkhovkv.space_app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.sterkhovkv.space_app.dto.SatelliteDataDTO;
import ru.sterkhovkv.space_app.dto.SatelliteTLEDTO;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CelestrakSatelliteServiceImpl implements SatelliteService {
    @Value("${celestrak.api.baseUrl}")
    private String baseUrl;
    @Value("${celestrak.api.url}")
    private String url;
    @Value("${celestrak.api.urlStations}")
    private String urlStationsList;
    @Value("${celestrak.api.urlSatellites}")
    private String urlSatellitesList;
    @Value("${celestrak.api.urlStationsTLE}")
    private String urlStationsListTLE;
    @Value("${celestrak.api.urlSatellitesTLE}")
    private String urlSatellitesListTLE;
    @Value("${celestrak.api.urlTLEISS}")
    private String urlTLEISS;

    private final WebClient webClient;

    @Autowired
    public CelestrakSatelliteServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Flux<SatelliteDataDTO> getSpaceStations() {
        return webClient.get()
                .uri(urlStationsList)
                .retrieve()
                .bodyToFlux(SatelliteDataDTO.class);
    }

    @Override
    public Mono<List<String>> getSpaceStationsNames() {
        return getSpaceStations()
                .map(SatelliteDataDTO::getObjectName)
                .collectList();
    }


    @Override
    public Flux<SatelliteDataDTO> getSatellites() {
        return webClient.get()
                .uri(urlSatellitesList)
                .retrieve()
                .bodyToFlux(SatelliteDataDTO.class);
    }

    @Override
    public Mono<List<String>> getSatelliteNames() {
        return getSatellites()
                .map(SatelliteDataDTO::getObjectName)
                .collectList();
    }

    @Override
    public Flux<SatelliteTLEDTO> getSpaceStationsTLE() {
        return webClient.get()
                .uri(urlStationsListTLE)
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(response -> Flux.fromIterable(parseTLEData(response)));
    }

    @Override
    public Flux<SatelliteTLEDTO> getSatellitesTLE() {
        return webClient.get()
                .uri(urlSatellitesListTLE)
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(response -> Flux.fromIterable(parseTLEData(response)));
    }

    @Override
    public Mono<List<SatelliteTLEDTO>> getTLEISS() {
        return webClient.get()
                .uri(urlTLEISS)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseTLEData);
    }


    public Mono<SatelliteDataDTO> getFirstSatellite() {
        return getSatellites()
                .next();
    }

    public Mono<SatelliteDataDTO> getSatelliteByName(String objectName) {
        URI requestUri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("NAME", objectName) // Прямо передаем objectName
                .queryParam("FORMAT", "json")
                .build() // Создаем UriComponents
                .toUri(); // Преобразуем в URI

        System.out.println("requestUrl = " + requestUri);

        return webClient.get()
                .uri(requestUri)
                .retrieve()
                .bodyToFlux(SatelliteDataDTO.class)
                .next();
    }


    private List<SatelliteTLEDTO> parseTLEData(String response) {
        List<SatelliteTLEDTO> satellites = new ArrayList<>();
        String[] lines = response.split("\n");

        for (int i = 0; i < lines.length; i += 3) {
            if (i + 2 < lines.length) {
                String name = lines[i].trim();
                String line1 = lines[i + 1].trim();
                String line2 = lines[i + 2].trim();
                satellites.add(new SatelliteTLEDTO(name, line1, line2));
            }
        }

        return satellites;
    }
}
