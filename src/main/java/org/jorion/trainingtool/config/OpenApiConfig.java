package org.jorion.trainingtool.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.TreeMap;

@Configuration
@SecurityScheme(
        name = "authBasic",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "Basic Authorization"
)
@OpenAPIDefinition(
        info = @Info(title = "TrainingTool REST API", version = "v1"),
        security = @SecurityRequirement(name = "authBasic"),
        servers = {@Server(url = "/trainingtool")}
)
public class OpenApiConfig {

    @Bean
    @SuppressWarnings("rawtypes")
    public OpenApiCustomiser sortSchemasAlphabetically() {

        return openApi -> {
            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            openApi.getComponents().setSchemas(new TreeMap<>(schemas));
        };
    }
}
