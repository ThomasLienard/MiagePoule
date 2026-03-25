package com.miage.pouleAPI.services;

import com.miage.pouleAPI.entity.Competition;
import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.repositories.EventRepository;
import com.miage.pouleAPI.services.interfaces.EventService;
import com.miage.pouleAPI.services.interfaces.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@EnableScheduling
public class SchedulerServiceImpl implements SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    private final EventRepository eventRepository;
    private final EventService eventService;

    public SchedulerServiceImpl(EventRepository eventRepository, EventService eventService) {
        this.eventRepository = eventRepository;
        this.eventService = eventService;
    }


    @Scheduled(fixedRate = 60000) // 60 secondes
    public void checkEventsStartingNow() {
        ZoneId zone = ZoneId.of("Europe/Paris");
        ZonedDateTime nowZoned = ZonedDateTime.now(zone);
        LocalDateTime now = nowZoned.toLocalDateTime();
        LocalDateTime nowAgo = now.minusSeconds(62);
        logger.info("now : " + now);

        // Évènements commencés depuis 5 min
        List<Event> startingEvents = eventRepository
                .findByTimeSlotStartBetween(nowAgo, now);

        logger.info("Checking events: {} starting now", startingEvents.size());

        for (Event event : startingEvents) {
            // On notifie automatiquement
            logger.info("=== before startEvent ===");
            eventService.startEvent(event.getId());
            logger.info("Notified start of event: {}", event.getName());
        }
    }


    @Scheduled(fixedRate = 60000)
    public void checkEventsForResults() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinAgo = now.minusMinutes(5);

        // Évènements finis depuis 5 min
        List<Event> finishedEvents = eventRepository
                .findByTimeSlotEndBetween(fiveMinAgo, now);

        for (Event event : finishedEvents) {
            eventService.startEvent(event.getId());
        }
    }
}
