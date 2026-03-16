import {agendaWithAllEvents, emptyAgenda} from "../fixtures/agenda";

describe('Tests - Agenda', () => {

    beforeEach(() => {
        // Désactive l'arrêt du test sur les exceptions non gérées
        cy.on('uncaught:exception', () => false);

        // Visite la page d'accueil
        cy.visit('/');
        cy.wait(2000);

        // Connexion en tant que commissaire
        cy.contains('Connexion').click();
        cy.wait(1000);

        // Remplir le formulaire de connexion
        cy.get('input[type="email"]').type('volontaire@example.com');
        cy.get('input[type="password"]').type('test123');

        // Soumettre le formulaire
        cy.get('button[type="submit"]').click();
        cy.wait(1000);
    });

    it('Scénario : Affichage de la page d\'agenda avec des tâches', () => {
        cy.intercept('GET', '**/volunteer/agenda',
            agendaWithAllEvents
        ).as('getAgenda')

        cy.contains('Mon agenda').click();
        cy.wait('@getAgenda');
        cy.wait(500);

        agendaWithAllEvents.forEach(task => {
            cy.contains(task.name)
        })

        const todayEvent = agendaWithAllEvents[0].event
        cy.contains(todayEvent.eventName).click()
        cy.wait(200)

        agendaWithAllEvents.slice(0, 2).forEach(task => {
            cy.contains(task.name).should("be.visible")
            cy.contains(task.description).should("be.visible")
        })

        const tomorrowEvent = agendaWithAllEvents[2].event
        cy.contains(tomorrowEvent.eventName).click()
        cy.wait(200)

        agendaWithAllEvents.slice(2, 4).forEach(task => {
            cy.contains(task.name).should("be.visible")
            cy.contains(task.description).should("be.visible")
        })
    })

    it('Scénario : Affichage de la page d\'agenda sans tâches', () => {
        cy.intercept('GET', '**/volunteer/agenda',
            emptyAgenda
        ).as('getAgenda')

        cy.contains('Mon agenda').click();
        cy.wait('@getAgenda');
        cy.wait(500);

        cy.contains("Aujourd'hui")
        cy.contains("Demain")
    })
})