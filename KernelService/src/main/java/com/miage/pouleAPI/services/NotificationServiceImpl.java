package com.miage.pouleAPI.services;

import com.miage.pouleAPI.entity.Event;
import com.miage.pouleAPI.entity.Notification;
import com.miage.pouleAPI.repositories.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements com.miage.pouleAPI.services.interfaces.NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void notifyEventStart(Event event) {
        Notification n = new Notification();
        n.setDescription("L'épreuve " + event.getName() + " va commencer.");
        n.setType(TypeNotification.INFO);
        n.setEvent(event);

        // on persiste la notif
        notificationRepository.save(n);

        // on notifie les observateurs de l'event
        event.getCompetition().notifyObservers(n);
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
