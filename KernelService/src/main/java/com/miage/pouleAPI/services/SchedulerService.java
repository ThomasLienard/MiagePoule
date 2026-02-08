package com.miage.pouleAPI.services;

// NOUVEAU fichier : src/main/java/com/miage/pouleAPI/services/EventScheduler.java

import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.repositories.EventRepository;
import com.miage.pouleAPI.services.interfaces.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    private final EventRepository eventRepository;
    private final EventService eventService;

    public SchedulerService(EventRepository eventRepository, EventService eventService) {
        this.eventRepository = eventRepository;
        this.eventService = eventService;
    }

    // Vérifie toutes les minutes les évènements qui commencent
    @Scheduled(fixedRate = 60000) // 60 secondes
    public void checkEventsStartingNow() {
        LocalDateTime now = LocalDateTime.now();

        // Évènements dont le début est passé mais la fin pas encore
        List<Event> startingEvents = eventRepository
                .findOngoingEvents(now);

        logger.info("Checking events: {} starting now", startingEvents.size());

        for (Event event : startingEvents) {
            // On notifie automatiquement
            eventService.startEvent(event.getId());
            logger.info("Notified start of event: {}", event.getName());
        }
    }

    // Également pour les résultats (ex : 5 min après la fin)
    @Scheduled(fixedRate = 60000)
    public void checkEventsForResults() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinAgo = now.minusMinutes(5);

        // Évènements finis depuis 5 min (à adapter selon tes besoins)
        List<Event> finishedEvents = eventRepository
                .findByTimeSlotEndBetween(fiveMinAgo, now);

        for (Event event : finishedEvents) {
            eventService.startEvent(event.getId());
        }
    }
}
