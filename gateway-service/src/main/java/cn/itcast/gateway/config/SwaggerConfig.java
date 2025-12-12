package cn.itcast.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("cn.itcast.gateway"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Gateway Service API")
                .description("网关服务接口文档")
                .contact(new Contact("ITCAST", "http://www.itcast.cn/", ""))
                .version("1.0")
                .build();
    }

    @Bean
    @Primary
    public SwaggerResourcesProvider swaggerResourcesProvider(RouteLocator routeLocator) {
        return () -> {
            List<SwaggerResource> resources = new ArrayList<>();
            
            // 添加各个微服务的Swagger资源配置
            resources.add(swaggerResource("account-service", "/account/v3/api-docs", "3.0"));
            resources.add(swaggerResource("order-service", "/order/v3/api-docs", "3.0"));
            resources.add(swaggerResource("storage-service", "/storage/v3/api-docs", "3.0"));
            resources.add(swaggerResource("auth-service", "/api/auth/v3/api-docs", "3.0"));
            
            return resources;
        };
    }

    private SwaggerResource swaggerResource(String name, String location, String version) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion(version);
        return swaggerResource;
    }
}