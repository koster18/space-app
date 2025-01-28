package ru.sterkhovkv.space_app.service.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WebClientServiceImpl implements WebClientService {
    private final WebClient webClient;

    @Override
    public <T, R> Mono<R> post(String uri, T request, Class<R> responseType) {
        return webClient.post()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorResume(e -> {
                    return Mono.error(new RuntimeException("Ошибка при выполнении POST-запроса: " + e.getMessage()));
                });
    }

    @Override
    public <R> Mono<R> get(String uri, Class<R> responseType) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorResume(e -> {
                    return Mono.error(new RuntimeException("Ошибка при выполнении GET-запроса: " + e.getMessage()));
                });
    }
}
