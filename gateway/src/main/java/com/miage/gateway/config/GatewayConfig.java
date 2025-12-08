package com.miage.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

import java.util.List;

@Configuration
public class GatewayConfig {

        @Value("${service.auth.url}")
        private String authServiceUrl;

        @Value("${service.kernel.url}")
        private String kernelServiceUrl;

        @Value("${service.map.url}")
        private String mapServiceUrl;

        @Value("${service.planning.url}")
        private String planningServiceUrl;

        @Value("${service.report.url}")
        private String reportServiceUrl;

        @Value("${service.alerts.url}")
        private String alertsServiceUrl;

        @Value("${service.crypto.url}")
        private String cryptoServiceUrl;

        // AuthService: endpoints for authentication/account creation
        @Bean
        public RouterFunction<ServerResponse> authServiceRoutes() {
                return route("auth_service")
                                .POST("/signup", http())
                                .POST("/login", http())
                                .POST("/logout", http())
                                .before(uri(authServiceUrl))
                                .build();
        }

        // KernelService: core user/account and event management
        @Bean
        public RouterFunction<ServerResponse> kernelServiceRoutes() {
                return route("kernel_service")
                                .GET("/account/ticket", http())
                                .GET("/account/personal-data", http())
                                .GET("/account/settings", http())
                                .PUT("/account/settings", http())
                                .GET("/account/settings/notifications", http())

                                // Athlete / Volunteer / Commissaire validation
                                .POST("/account/athlete/validation", http())
                                .POST("/account/volunteer/validation", http())
                                .POST("/account/superintendant/validation", http())

                                .GET("/public/events", http())
                                .GET("/my-events", http())
                                .POST("/my-events/forfeit", http())

                                // Championship / events management used by commissaire / responsable
                                .POST("/championship/create", http())
                                .PUT("/championship/{champId}/update", http())
                                .POST("/championship/{champId}/events/create", http())
                                .PUT("/championship/{champId}/events/{eventId}/update", http())
                                .DELETE("/championship/{champId}/events/{eventId}/delete", http())
                                .GET("/public/championship/{champId}/comp/{compId}/events/{eventId}", http())

                                .GET("/public/championship/{champId}/events/{eventId}/athletes", http())
                                .PUT("/championship/{champId}/events/{eventId}/athletes/{athleteId}/update", http())
                                .POST("/championship/{champId}/events/{eventId}/athletes/add", http())
                                .POST("/championship/{champId}/events/{eventId}/results/update", http())

                                .POST("/championship/{champId}/map/fanzone/add", http())
                                .PUT("/championship/{champId}/map/fanzone/{fanzoneId}/update", http())

                                .PUT("/championship/{champId}/events/{eventId}/update", http())
                                .DELETE("/championship/{champId}/events/{eventId}/delete", http())

                                .before(uri(kernelServiceUrl))
                                .build();
        }

        // MapService: maps, geolocation and public maps
        @Bean
        public RouterFunction<ServerResponse> mapServiceRoutes() {
                return route("map_service")
                                .GET("/public/map", http())
                                .GET("/championship/{champId}/super-map", http())
                                .GET("/public/championship/{champId}/map/fanzone", http())
                                .GET("/public/championship/{champId}/map/fanzone/{fanzoneId}", http())
                                .before(uri(mapServiceUrl))
                                .build();
        }

        // PlanningService: agenda and ics exports/management
        @Bean
        public RouterFunction<ServerResponse> planningServiceRoutes() {
                return route("planning_service")
                                .GET("/agenda", http())
                                .POST("/agenda/add", http())
                                .POST("/championship/{champId}/events/create", http())
                                .before(uri(planningServiceUrl))
                                .build();
        }

        // ReportService: reports and statistics
        @Bean
        public RouterFunction<ServerResponse> reportServiceRoutes() {
                return route("report_service")
                                .GET("/report", http())
                                .POST("/events/{eventId}/notifications/create", http())
                                .before(uri(reportServiceUrl))
                                .build();
        }

        // Alerts / Notifications service
        @Bean
        public RouterFunction<ServerResponse> alertServiceRoutes() {
                return route("alert_service")
                                .POST("/events/{eventId}/notifications/create", http())
                                .POST("/notifications/send", http())
                                .before(uri(alertsServiceUrl))
                                .build();
        }

        // Crypto service: encrypt / decrypt (example placeholders)
        @Bean
        public RouterFunction<ServerResponse> cryptoServiceRoutes() {
                return route("crypto_service")
                                .POST("/crypto/encrypt", http())
                                .POST("/crypto/decrypt", http())
                                .before(uri(cryptoServiceUrl))
                                .build();
        }

        // ================= CORS GLOBAL =================
        @Bean
        public CorsFilter corsFilter() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowCredentials(true);
                config.setAllowedOrigins(List.of("*"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);

                return new CorsFilter(source);
        }

}
