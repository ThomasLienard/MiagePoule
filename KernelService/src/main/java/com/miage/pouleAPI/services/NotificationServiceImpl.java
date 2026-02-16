package com.miage.pouleAPI.services;

import com.miage.pouleAPI.dtos.NotificationDTO;
import com.miage.pouleAPI.entity.*;
import com.miage.pouleAPI.repositories.CompetitionObserverRepository;
import com.miage.pouleAPI.repositories.NotificationRepository;
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


        System.out.println("=============== before save ");
        // on persiste la notif
        notificationRepository.save(n);
        System.out.println("=============== after save ");

        System.out.println("=============== before notifyObservers ");
        // on notifie les observateurs de l'event
        //event.getCompetition().notifyObservers(n);
        System.out.println("=============== after notifyObservers ");


        System.out.println("=============== before dto ");
        // Ici : création du DTO + envoi SSE
        NotificationDTO dto = NotificationDTO.fromEntity(n);
        System.out.println("=============== after dto ");

        System.out.println("=============== before getObservers ");
        Collection<CompetitionObserver> observers = competitionObserverRepository.findByCompetition(event.getCompetition());
        System.out.println(observers.toString());
        System.out.println("=============== after getObservers ");

        for (CompetitionObserver observer : observers) {
            System.out.println("=============== observer " + observer.getId().getUserId());
            sseNotificationService.sendNotification(observer.getId().getUserId(), dto);
        }
    }

    /**
     * Pour le moment prévient juste que les résultats sont dispo,
     * les résultat ne sont pas encore implémentés
     * @param event l'évènement concerné par la notif
     */
    @Override
    public void notifyEventResults(Event event) {
        Notification n = new Notification();
        n.setDescription("Les résultats de " + event.getName() + " sont disponibles.");
        n.setType(TypeNotification.RESULT);
        n.setEvent(event);

        notificationRepository.save(n);
        event.getCompetition().notifyObservers(n);
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
