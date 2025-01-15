package ru.sterkhovkv.space_app.service.external;

import reactor.core.publisher.Mono;

public interface WebClientService {

    <T, R> Mono<R> post(String uri, T request, Class<R> responseType);

    <T> Mono<T> get(String uri, Class<T> responseType);
}
