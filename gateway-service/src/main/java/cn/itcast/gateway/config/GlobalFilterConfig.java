package cn.itcast.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class GlobalFilterConfig {

    @Bean
    @Order(-1)
    public GlobalFilter corsFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // 检查是否是预检请求
            if (CorsUtils.isPreFlightRequest(request)) {
                ServerHttpResponse response = exchange.getResponse();
                HttpHeaders headers = response.getHeaders();
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
                headers.add("Access-Control-Allow-Headers", "*");
                headers.add("Access-Control-Max-Age", "3600");
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }
            
            // 检查Swagger相关路径
            String path = request.getPath().value();
            if (path.contains("/v3/api-docs") || path.contains("/swagger") || path.contains("/webjars")) {
                // 移除Authorization头，避免传递给下游服务
                ServerHttpRequest modifiedRequest = request.mutate()
                        .headers(httpHeaders -> {
                            httpHeaders.remove("Authorization");
                        })
                        .build();
                
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();
                
                return chain.filter(modifiedExchange);
            }
            
            return chain.filter(exchange);
        };
    }
}