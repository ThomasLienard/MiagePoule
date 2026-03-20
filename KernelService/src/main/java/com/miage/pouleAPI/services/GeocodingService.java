package com.miage.pouleAPI.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GeocodingService {
    private final String USER_AGENT = "PouleAPI-Lille-StudentProject/1.0";
    private RestTemplate restTemplate;

    public GeocodingService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public Double[] getCoordinates(String address) {
        if (address == null || address.isBlank()) return new Double[]{48.8566, 2.3522};

        Double[] coords = executeQuery(address);

        // Si échec, on tente le Fallback (Ville + Code Postal uniquement)
        if (coords[0] == 0.0 && coords[1] == 0.0) {
            System.out.println("Échec adresse précise, tentative de repli sur la ville...");
            coords = fallbackSearch(address);
        }

        return coords;
    }

    private Double[] fallbackSearch(String address) {
        // On essaie d'extraire la fin de l'adresse (souvent "59650 Villeneuve d'Ascq")
        String[] parts = address.split(",");
        String cityPart = parts[parts.length - 1].trim();

        return executeQuery(cityPart);
    }

    private Double[] executeQuery(String query) {
        try {
            String url = UriComponentsBuilder.fromUriString("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            JsonNode response = responseEntity.getBody();

            if (response != null && response.isArray() && !response.isEmpty()) {
                return new Double[]{
                        response.get(0).get("lat").asDouble(),
                        response.get(0).get("lon").asDouble()
                };
            }
        } catch (Exception e) {
            System.err.println("Erreur API pour '" + query + "' : " + e.getMessage());
        }
        return new Double[]{0.0, 0.0};
    }
}