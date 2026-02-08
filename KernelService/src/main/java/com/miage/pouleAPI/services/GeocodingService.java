package com.miage.pouleAPI.services;

import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class GeocodingService {
    private final RestTemplate restTemplate = new RestTemplate();

    public Double[] getCoordinates(String address) {
        try {
            // API Nominatim (OpenStreetMap)
            String url = "https://nominatim.openstreetmap.org/search?q=" + address + "&format=json&limit=1";
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.isArray() && !response.isEmpty()) {
                double lat = response.get(0).get("lat").asDouble();
                double lon = response.get(0).get("lon").asDouble();
                return new Double[]{lat, lon};
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return new Double[]{0.0, 0.0}; 
    }
}