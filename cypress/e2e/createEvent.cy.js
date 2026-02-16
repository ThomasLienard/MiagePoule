describe('Page de Création d\'Évènement', () => {
    beforeEach(() => {
        // Étape de connexion préalable
        cy.visit('/login');
        cy.get('input[placeholder="Email"]').type('anna@example.com');
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.get('button[type="submit"]').click();
        cy.wait(1000);
        // 1. On intercepte les appels au chargement
        cy.intercept('GET', '**/public/championship', { fixture: 'championships.json' }).as('getChampionships');
        cy.intercept('GET', '**/public/championship/1/comp', { fixture: 'competitions.json' }).as('getCompetitions');

        // 2. On intercepte le POST de création
        cy.intercept('POST', '**/admin/events', {
            statusCode: 201,
            body: { message: 'Success' }
        }).as('createEvent');

        // 3. On visite la page
        cy.visit('/admin/create-event/');
        cy.wait(1000);
    });

    it('devrait afficher des erreurs de validation si le formulaire est vide', () => {
        cy.get('button[type="submit"]').click();
        cy.wait(1000);
        // Vérifie que les messages d'erreur Bootstrap apparaissent
        cy.get('.invalid-feedback').should('be.visible');
        cy.get('form').should('have.class', 'was-validated');
        cy.wait(1000);
    });

    it('devrait bloquer l\'envoi si la date de fin est avant la date de début', () => {
        cy.wait('@getChampionships');
        cy.get('select').first().select('World Cup');
        cy.wait('@getCompetitions');
        cy.get('select[name="competitionId"]').select('100m Sprint');

        cy.get('input[name="name"]').type('Event Test');
        cy.get('textarea[name="description"]').type('Description de l\'épreuve olympique');

        cy.get('input[name="startTime"]').type('2025-12-01T10:00');
        cy.get('input[name="endTime"]').type('2025-10-01T08:00');

        cy.get('input[name="placeName"]').type('Stade de France');
        cy.get('input[name="number"]').type('3');
        cy.get('input[name="street"]').type('Avenue de la Victoire');
        cy.get('input[name="zipCode"]').type('93200');
        cy.get('input[name="city"]').type('Saint-Denis');
        cy.wait(1000);

        cy.get('button[type="submit"]').click();
        cy.wait(1000);
        cy.get('.alert')
            .should('be.visible')
            .and('contain', 'La date de fin doit être strictement après la date de début');
        cy.wait(1000);
    });

    it('devrait créer un évènement avec succès et rediriger', () => {
        cy.wait('@getChampionships');
        cy.get('select').first().select('World Cup');
        cy.wait('@getCompetitions');
        cy.get('select[name="competitionId"]').select('100m Sprint');
        cy.wait(1000);
        cy.get('input[name="name"]').type('Ma Super Finale');
        cy.get('select[name="typeEventName"]').select('TRIAL');
        cy.get('textarea[name="description"]').type('Description de l\'épreuve olympique');
        cy.get('input[name="startTime"]').type('2025-12-01T14:00');
        cy.get('input[name="endTime"]').type('2025-12-01T16:00');
        cy.wait(1000);
        cy.get('input[name="placeName"]').type('Stade de France');
        cy.get('input[name="number"]').type('3');
        cy.get('input[name="street"]').type('Avenue de la Victoire');
        cy.get('input[name="zipCode"]').type('93200');
        cy.get('input[name="city"]').type('Saint-Denis');
        cy.wait(1000);
        cy.get('#hasParking').check({ force: true });

        cy.get('button[type="submit"]').click();
        cy.wait(1000);
        cy.wait('@createEvent').its('request.body').should('include', {
            name: 'Ma Super Finale',
            competitionId: '1',
            hasParking: true
        });

        cy.get('.alert-success').should('contain', 'Évènement planifié avec succès');
        cy.wait(1000);
        cy.url().should('include', '/admin');
    });
});