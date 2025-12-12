package com.map.MapService.service;

import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PublicMapService {

    private final EventRepository eventRepository;
    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PublicMapService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<PublicEventMapDto> getPublicEventsForMap() {
        return eventRepository.findAllPublicEvents()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private PublicEventMapDto toDto(Event e) {
        Place p = e.getPlace();
        TimeSlot t = e.getTimeSlot();

        String start = t.getStartTime() != null ? t.getStartTime().format(timeFormatter) : "";
        String end   = t.getEndTime() != null ? t.getEndTime().format(timeFormatter) : "";

        return new PublicEventMapDto(
                e.getId(),
                e.getName(),
                e.getCompetition().getName(),
                p.getName(),
                p.getCity(),
                p.getStreet(),
                p.getLatitude(),
                p.getLongitude(),
                start,
                end
        );
    }
}