package cn.itcast.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class SwaggerResourceConfig {

    @Bean
    public RouterFunction<ServerResponse> swaggerRouter() {
        return RouterFunctions.route()
                .GET("/", request -> 
                    ServerResponse.status(HttpStatus.MOVED_PERMANENTLY)
                            .header("Location", "/swagger-ui/")
                            .build())
                .build();
    }
}