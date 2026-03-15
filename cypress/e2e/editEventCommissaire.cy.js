describe("Tests - Modification de la date d'un évènement par un commissaire", () => {

    const trialsFixturePath = 'editEventCommissaire/allTrials.json'
    const resultFixturePath = 'editEventCommissaire/allResults.json'

    beforeEach(() => {
        // Désactive l'arrêt du test sur les exceptions non gérées
        cy.on('uncaught:exception', () => false);

        // Visite la page d'accueil
        cy.visit('/');
        cy.wait(1000);

        // Connexion en tant que athlete
        cy.contains('Connexion').click();
        cy.wait(1000);

        // Remplir le formulaire de connexion
        cy.get('input[type="email"]').type('commissaire@example.com');
        cy.wait(500);
        cy.get('input[type="password"]').type('test123');
        cy.wait(500);

        // Soumettre le formulaire
        cy.get('button[type="submit"]').click();
        cy.wait(500);

        cy.intercept('GET','**/commissaire/trials',
            {fixture: trialsFixturePath}
        ).as('getTrials')

        cy.intercept('GET','**/commissaire/trials/1/results',
            {fixture: resultFixturePath}
        ).as('getResults')

        // Naviguer vers la page de gestion des épreuves
        cy.contains('Gestion épreuves').click();
        cy.wait(500);
        cy.wait("@getTrials");
        cy.wait("@getResults");

    });

    it('Scénario : Modification de la date d\'une épreuve', () => {
        const championshipFixturePath = 'editEventCommissaire/allChampionships.json'
        const allEventsFixturePath = 'editEventCommissaire/allEvents.json'
        const competitionsFixturePath = 'editEventCommissaire/allCompetitions.json'
        const eventFixturePath = 'editEventCommissaire/targetedCompetition.json'

        cy.intercept('GET','**/public/championship',
            {fixture: championshipFixturePath}
        ).as('getChampionships')
        cy.intercept('GET','**/public/events',
            {fixture: allEventsFixturePath}
        ).as('getEvents')
        cy.intercept('GET','**/public/championship/1/comp',
            {fixture: competitionsFixturePath}
        ).as('getCompetitions')
        cy.intercept('GET','**/public/events/1',
            {fixture: eventFixturePath}
        ).as('getEventDetail')

        // Cliquer sur le bouton pour gérer une épreuve
        cy.get('button').contains("Modifier date").first().click();
        cy.wait("@getEvents")
        cy.wait("@getChampionships")
        cy.wait("@getCompetitions")
        cy.wait("@getEventDetail")


        cy.contains("Début")
            .parent()
            .get("input[name='startTime']")
            .type("2025-03-21T10:00:00")

        cy.contains("Fin")
            .parent()
            .get("input[name='endTime']")
            .type("2026-06-21T10:30:00")


        cy.intercept('PUT', '**/commissaire/events/1',
            {statusCode:201}
        ).as('updateEvent')

        cy.contains("Enregistrer les modifications")
            .click()

        cy.wait("@updateEvent")

        cy.wait(2000)

        cy.url().should('match', /\/commissaire\/trials/);
    });

    it('Scénario : Modification de la date d\'une épreuve | date_start > date_end', () => {
        const championshipFixturePath = 'editEventCommissaire/allChampionships.json'
        const allEventsFixturePath = 'editEventCommissaire/allEvents.json'
        const competitionsFixturePath = 'editEventCommissaire/allCompetitions.json'
        const eventFixturePath = 'editEventCommissaire/targetedCompetition.json'

        cy.intercept('GET', '**/public/championship',
            {fixture: championshipFixturePath}
        ).as('getChampionships')
        cy.intercept('GET', '**/public/events',
            {fixture: allEventsFixturePath}
        ).as('getEvents')
        cy.intercept('GET', '**/public/championship/1/comp',
            {fixture: competitionsFixturePath}
        ).as('getCompetitions')
        cy.intercept('GET', '**/public/events/1',
            {fixture: eventFixturePath}
        ).as('getEventDetail')

        // Cliquer sur le bouton pour gérer une épreuve
        cy.get('button').contains("Modifier date").first().click();
        cy.wait("@getEvents")
        cy.wait("@getChampionships")
        cy.wait("@getCompetitions")
        cy.wait("@getEventDetail")


        cy.contains("Début")
            .parent()
            .get("input[name='startTime']")
            .type("2025-03-21T10:00:00")

        cy.contains("Fin")
            .parent()
            .get("input[name='endTime']")
            .type("2020-06-21T10:30:00")


        cy.contains("Enregistrer les modifications")
            .click()

        cy.wait(2000)

        cy.url().should('match', /\/commissaire\/update-event/);
    })

    it('Scénario : Modification de la date d\'une épreuve | date_end_trial > date_end_competition', () => {
        const championshipFixturePath = 'editEventCommissaire/allChampionships.json'
        const allEventsFixturePath = 'editEventCommissaire/allEvents.json'
        const competitionsFixturePath = 'editEventCommissaire/allCompetitions.json'
        const eventFixturePath = 'editEventCommissaire/targetedCompetition.json'

        cy.intercept('GET', '**/public/championship',
            {fixture: championshipFixturePath}
        ).as('getChampionships')
        cy.intercept('GET', '**/public/events',
            {fixture: allEventsFixturePath}
        ).as('getEvents')
        cy.intercept('GET', '**/public/championship/1/comp',
            {fixture: competitionsFixturePath}
        ).as('getCompetitions')
        cy.intercept('GET', '**/public/events/1',
            {fixture: eventFixturePath}
        ).as('getEventDetail')

        // Cliquer sur le bouton pour gérer une épreuve
        cy.get('button').contains("Modifier date").first().click();
        cy.wait("@getEvents")
        cy.wait("@getChampionships")
        cy.wait("@getCompetitions")
        cy.wait("@getEventDetail")


        cy.contains("Début")
            .parent()
            .get("input[name='startTime']")
            .type("2025-03-21T10:00:00")

        cy.contains("Fin")
            .parent()
            .get("input[name='endTime']")
            .type("2027-06-06T10:30:00")


        cy.contains("Enregistrer les modifications")
            .click()

        cy.wait(2000)

        cy.url().should('match', /\/commissaire\/update-event/);
    })

});