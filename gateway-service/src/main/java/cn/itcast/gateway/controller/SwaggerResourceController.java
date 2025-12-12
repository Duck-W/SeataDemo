package cn.itcast.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.swagger.web.*;

import java.util.List;

@RestController
public class SwaggerResourceController {

    @Autowired(required = false)
    private SwaggerResourcesProvider swaggerResourcesProvider;

    @GetMapping("/swagger-resources")
    public ResponseEntity<List<SwaggerResource>> swaggerResources() {
        if (swaggerResourcesProvider == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(swaggerResourcesProvider.get());
    }

    @GetMapping("/swagger-resources/configuration/ui")
    public ResponseEntity<UiConfiguration> uiConfiguration() {
        return ResponseEntity.ok(UiConfigurationBuilder.builder().build());
    }

    @GetMapping("/swagger-resources/configuration/security")
    public ResponseEntity<SecurityConfiguration> securityConfiguration() {
        return ResponseEntity.ok(SecurityConfigurationBuilder.builder().build());
    }
}