package com.miage.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class GatewayConfig {

        @Value("${service.backend.url}")
        private String backendUrl;

        @Bean
        public RouterFunction<ServerResponse> backendRoutes() {
                return route("backend_service")
                        .GET("/**", http())
                        .POST("/**", http())
                        .PUT("/**", http())
                        .DELETE("/**", http())
                        .PATCH("/**", http())
                        .before(uri(backendUrl))
                        .build();
        }
}
