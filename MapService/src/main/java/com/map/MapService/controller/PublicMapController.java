package com.map.MapService.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PublicMapController {

    private final PublicMapService publicMapService;

    public PublicMapController(PublicMapService publicMapService) {
        this.publicMapService = publicMapService;
    }

    @GetMapping("/public/map")
    public List<PublicEventMapDto> getPublicMap() {
        return publicMapService.getPublicEventsForMap();
    }
}
