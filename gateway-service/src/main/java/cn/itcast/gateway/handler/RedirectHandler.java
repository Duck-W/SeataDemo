package cn.itcast.gateway.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class RedirectHandler {

    public Mono<ServerResponse> redirectToSwagger(ServerRequest request) {
        return ServerResponse
                .status(HttpStatus.TEMPORARY_REDIRECT)
                .header("Location", "/swagger-ui/index.html")
                .build();
    }
}