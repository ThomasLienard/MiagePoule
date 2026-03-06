package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.NotificationDTO;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.CompetitionObserverRepository;
import com.miage.pouleAPI.repositories.NotificationRepository;
import com.miage.pouleAPI.repositories.SeverityRepository;
import com.miage.pouleAPI.services.interfaces.SseNotificationService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collection;

@Service
public class NotificationServiceImpl implements com.miage.pouleAPI.services.interfaces.NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseNotificationService sseNotificationService;
    private final SeverityRepository severityRepository;
    private final CompetitionObserverRepository competitionObserverRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, SseNotificationService sseNotificationService, SeverityRepository severityRepository, CompetitionObserverRepository competitionObserverRepository) {
        this.notificationRepository = notificationRepository;
        this.sseNotificationService = sseNotificationService;
        this.severityRepository = severityRepository;
        this.competitionObserverRepository = competitionObserverRepository;
    }

    @Override
    public void notifyEventStart(Event event) {
        Notification n = new Notification();
        n.setDescription("L'épreuve " + event.getName() + " va commencer.");
        n.setType(TypeNotification.INFO);
        n.setEvent(event);
        n.setEmissionDate(LocalDateTime.now());

        Severity sev = new Severity("info", "start event");
        severityRepository.save(sev);
        n.setSeverity(sev);


        // on persiste la notif
        notificationRepository.save(n);

        // on notifie les observateurs de l'event
        //event.getCompetition().notifyObservers(n);


        // Ici : création du DTO + envoi SSE
        NotificationDTO dto = NotificationDTO.fromEntity(n);

        Collection<CompetitionObserver> observers = competitionObserverRepository.findByCompetition(event.getCompetition());
        System.out.println(observers.toString());

        for (CompetitionObserver observer : observers) {
            sseNotificationService.sendNotification(observer.getId().getUserId(), dto);
        }
    }

    /**
     * Notifie les utilisateurs abonnés à la compétition que les résultats d'un événement sont disponibles
     * @param event l'évènement concerné par la notif
     */
    @Override
    public void notifyEventResults(Event event) {
        Notification n = new Notification();
        n.setDescription("Les résultats de " + event.getName() + " sont disponibles.");
        n.setType(TypeNotification.RESULT);
        n.setEvent(event);
        n.setEmissionDate(LocalDateTime.now());

        Severity sev = new Severity("info", "results available");
        severityRepository.save(sev);
        n.setSeverity(sev);

        // Persiste la notification
        notificationRepository.save(n);

        // Récupère les observateurs de la compétition
        Collection<CompetitionObserver> observers = competitionObserverRepository.findByCompetition(event.getCompetition());

        // Crée le DTO et envoie à tous les observateurs
        NotificationDTO dto = NotificationDTO.fromEntity(n);
        for (CompetitionObserver observer : observers) {
            sseNotificationService.sendNotification(observer.getId().getUserId(), dto);
        }
    }

//    public void notifySecurityIncident(Place place, String message, Severity severity) {
//        Notification n = new Notification();
//        n.setDescription(message);
//        n.setType(TypeOfNotification.SECURITY);
//        n.setPlace(place);
//        // éventuellement utiliser Severity dans la description ou un champ dédié
//
//        notificationRepository.save(n);
//        place.notifyObservers(n);
//    }
}
