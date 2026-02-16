describe('Page de Création de Championnat', () => {
    beforeEach(() => {
        // Étape de connexion préalable
        cy.visit('/login');
        cy.get('input[placeholder="Email"]').type('anna@example.com');
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.get('button[type="submit"]').click();
        cy.wait(2000);

        // On intercepte le POST de création
        cy.intercept('POST', '**/admin/champs', {
            statusCode: 201,
            body: { message: 'Success' }
        }).as('createChamp');

        // On visite la page
        cy.visit('/admin/create-champ/');
        cy.wait(1000);
    });

    it('devrait afficher des erreurs de validation si le formulaire est vide', () => {
        cy.get('button[type="submit"]').click();
        cy.wait(1000);

        cy.get('button[type="submit"]').click();

        cy.get('.invalid-feedback').should('be.visible');
        cy.get('form').should('have.class', 'was-validated');
        cy.wait(1000);
    });

    it('devrait bloquer l\'envoi si la date de fin est avant la date de début', () => {

        cy.get('input[name="name"]').type('Championship Test');
        cy.get('textarea[name="description"]').type('Description');

        cy.get('input[name="start"]').type('2025-12-01');
        cy.get('input[name="end"]').type('2025-10-01');

        cy.wait(1000);

        cy.get('button[type="submit"]').click();

        cy.wait(1000);
        cy.get('.alert')
            .should('be.visible')
            .and('contain', 'La date de fin doit être strictement après la date de début');
        cy.wait(1000);
    });

    it('devrait créer un évènement avec succès et rediriger', () => {
        cy.get('input[name="name"]').type('Ma Super Finale');
        cy.get('textarea[name="description"]').type('Description');
        cy.get('input[name="start"]').type('2025-12-01');
        cy.get('input[name="end"]').type('2026-12-01');
        cy.wait(1000);

        cy.get('button[type="submit"]').click();
        cy.wait(1000);
        cy.wait('@createChamp').its('request.body').should('include', {
            name: 'Ma Super Finale'
        });

        cy.get('.alert-success').should('contain', 'Championnat planifié avec succès');
        cy.wait(1000);
        cy.url().should('include', '/admin');
    });
});