describe('Tests - Annulation d\'une épreuve', () => {

    const resultsFixturePath = "cancel-trial/allResults.json"
    const results2FixturePath = "cancel-trial/allResults2.json"
    const trialsFixturePath = "cancel-trial/allTrials.json"

    beforeEach(() => {
        // Désactive l'arrêt du test sur les exceptions non gérées
        cy.on('uncaught:exception', () => false);

        // Visite la page d'accueil
        cy.visit('/');
        cy.wait(1500);

        // Connexion en tant que commissaire
        cy.contains('Connexion').click();
        cy.wait(1500);

        // Remplir le formulaire de connexion
        cy.get('input[type="email"]').type('commissaire@example.com');
        cy.get('input[type="password"]').type('test123');

        // Soumettre le formulaire
        cy.get('button[type="submit"]').click();
        cy.wait(1000);

        cy.intercept('GET', `**/commissaire/trials`,
            {fixture: trialsFixturePath}
        ).as('getTrials');

        cy.intercept('GET', `**/commissaire/trials/1/results`,
            {fixture: resultsFixturePath}
        ).as('getResults');

        cy.intercept('GET', `**/commissaire/trials/2/results`,
            {fixture: results2FixturePath}
        ).as('getResults2');

        // Naviguer vers la page de gestion des équipes
        cy.contains('Gestion épreuves').click();

        cy.wait('@getTrials');
        cy.wait('@getResults');
        cy.wait('@getResults2');
        cy.wait(500);
    });

    it('Scénario : Annulation d\'une épreuve', () => {
        cy.contains("Annuler")
            .first()
            .click();

        cy.get('.modal').within(() => {
            cy.get('textarea')
                .type("Manque de matériel");

            cy.intercept('PATCH', '**/commissaire/events/1/cancel',
                {statusCode: 204}
            ).as('cancelTrial')

            cy.contains("Confirmer l'annulation")
                .click();
        });
        cy.wait('@cancelTrial')
        cy.get('.modal').should('not.exist');
    })

    it('Scénario : Annulation d\'une épreuve - Echec', () => {
        cy.contains("Annuler")
            .first()
            .click();

        cy.get('.modal').within(() => {
            const stub = cy.stub()

            cy.on('window:alert', stub);

            cy.contains("Confirmer l'annulation")
                .click()
                .then(() => {
                    expect(stub.getCall(0)).to.be.calledWith('Veuillez saisir une raison pour l\'annulation.')
                });
        });
        cy.get('.modal').should('be.visible');
    })
});