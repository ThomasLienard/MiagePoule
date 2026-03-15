describe("Tests - Visualisation des épreuves d'un sportif connecté", () => {

    const eventsFixturePath = 'editEventAdmin/allEvents.json'
    const championshipFixturePath = 'editEventAdmin/allChampionships.json'
    const eventDetailFixturePath = 'editEventAdmin/eventDetail.json'
    const competitionsFixturePath = 'editEventAdmin/allCompetitions.json'

    beforeEach(() => {
        // Désactive l'arrêt du test sur les exceptions non gérées
        cy.on('uncaught:exception', () => false);

        // Visite la page d'accueil
        cy.visit('/');
        cy.wait(2000);

        // Connexion en tant que athlete
        cy.contains('Connexion').click();
        cy.wait(2000);

        // Remplir le formulaire de connexion
        cy.get('input[type="email"]').type('anna@example.com');
        cy.wait(500);
        cy.get('input[type="password"]').type('test123');
        cy.wait(500);

        // Soumettre le formulaire
        cy.get('button[type="submit"]').click();
        cy.wait(3000);

        cy.contains('Administration').click();
        cy.wait(500);

        cy.intercept('GET','**/public/championship',
            {fixture: championshipFixturePath}
        ).as('getChampionships')

        cy.intercept('GET','**/public/events',
            {fixture: eventsFixturePath}
        ).as('getEvents')

        cy.contains('Modifier un évènement').click();
        cy.wait("@getChampionships");
        cy.wait("@getEvents");
        cy.wait(500);
    });

    it('Scénario : Modification d\'une épreuve', () => {
        cy.intercept('GET','**/public/events/1',
            {fixture: eventDetailFixturePath}
        ).as('getEventDetail')

        cy.intercept('GET','**/public/championship/1/comp',
            {fixture: competitionsFixturePath}
        ).as('getAllCompetitions')

        cy.get("select[id='selectEvent']")
            .select(1)

        cy.wait("@getEventDetail");
        cy.wait("@getAllCompetitions");

        cy.get("select[id='selectChampionat']")
            .select(1)

        cy.get("select[id='selectCompetition']")
            .select(1)

        cy.get("input[name='startTime']")
            .type("2025-03-21T10:00:00")

        cy.get("input[name='endTime']")
            .type("2026-06-21T10:30:00")

        cy.get("input[id='eventName']")
            .type("100m Trial Heat 2")

        cy.intercept('PUT', '**/admin/events/1',
            {statusCode:201}
        ).as('updateEvent')

        cy.contains("Enregistrer les modifications")
            .click()

        cy.wait("@updateEvent")

        cy.wait(2000)

        cy.url().should('match', /\/admin/);
    });

    it('Scénario : Modification d\'une épreuve - Echec', () => {
        cy.intercept('GET','**/public/events/1',
            {fixture: eventDetailFixturePath}
        ).as('getEventDetail')

        cy.intercept('GET','**/public/championship/1/comp',
            {fixture: competitionsFixturePath}
        ).as('getAllCompetitions')

        cy.get("select[id='selectEvent']")
            .select(1)

        cy.wait("@getEventDetail");
        cy.wait("@getAllCompetitions");

        cy.get("input[id='eventName']")
            .clear()

        cy.contains("Enregistrer les modifications")
            .click()

        cy.wait(2000)

        cy.url().should('match', /\/admin\/update-event/);
    });

});