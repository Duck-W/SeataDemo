package cn.itcast.gateway.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestController
public class SwaggerUIController {

    @GetMapping(value = {"/swagger-ui/", "/swagger-ui/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public Mono<ResponseEntity<String>> swaggerUI() {
        return Mono.fromCallable(() -> {
            try {
                ClassPathResource resource = new ClassPathResource("META-INF/resources/webjars/springfox-swagger-ui/index.html");
                
                // 使用Java 8兼容的方式读取InputStream
                String content;
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    content = reader.lines().collect(Collectors.joining("\n"));
                }
                
                // 移除Swagger UI中的默认URL配置
                // Springfox会自动从/swagger-resources获取资源列表，需要移除默认的petstore URL
                content = content.replace("https://petstore.swagger.io/v2/swagger.json", "");
                
                // 如果HTML中包含url配置，需要移除或替换
                // 查找并移除url: "https://petstore.swagger.io/v2/swagger.json"这样的配置
                content = content.replaceAll("url\\s*:\\s*[\"']https://petstore\\.swagger\\.io/v2/swagger\\.json[\"']", "");
                content = content.replaceAll("\"url\"\\s*:\\s*[\"']https://petstore\\.swagger\\.io/v2/swagger\\.json[\"']", "");
                
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(content);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).build();
            }
        });
    }
}