package com.miage.pouleAPI.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.containsString;

@RestClientTest(GeocodingService.class)
class GeocodingServiceTest {

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void shouldReturnCoordinates_WhenAddressIsValid() {
        // Simulation de la réponse JSON de Nominatim
        String mockResponse = "[{\"lat\": \"50.6233\", \"lon\": \"3.1444\"}]";

        this.server.expect(requestTo(containsString("nominatim.openstreetmap.org")))
                .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        Double[] coords = geocodingService.getCoordinates("261 Boulevard de Tournai 59650 Villeneuve d'Ascq");

        assertEquals(50.6233, coords[0]);
        assertEquals(3.1444, coords[1]);
    }

    @Test
    void shouldReturnDefault_WhenApiFails() {
        this.server.expect(requestTo(containsString("q=Adresse,%20Inexistante")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        this.server.expect(requestTo(containsString("q=Inexistante")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Double[] coords = geocodingService.getCoordinates("Adresse, Inexistante");

        // Vérifie qu'après deux échecs, on a bien les coordonnées par défaut
        assertEquals(0.0, coords[0]);
    }
}