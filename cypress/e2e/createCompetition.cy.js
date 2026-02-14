describe('Page de Création de Compétition', () => {
    beforeEach(() => {
        // Étape de connexion préalable
        cy.visit('/login');
        cy.get('input[placeholder="Email"]').type('anna@example.com');
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.get('button[type="submit"]').click();
        cy.wait(2000);

        cy.intercept('GET', '**/public/championship', { fixture: 'championships.json' }).as('getChamps');

        cy.intercept('POST', '**/admin/comps', {
            statusCode: 201,
            body: { message: 'Success' }
        }).as('createComp');

        cy.visit('/admin/create-comp/');
        cy.wait('@getChamps');
    });

    it('devrait afficher des erreurs de validation si le formulaire est vide', () => {
        cy.get('button[type="submit"]').click();

        // Vérifie que les messages d'erreur Bootstrap apparaissent
        cy.get('.invalid-feedback').should('be.visible');
        cy.get('form').should('have.class', 'was-validated');
    });

    it('devrait bloquer l\'envoi si la date de fin est avant la date de début', () => {
        cy.get('select[name="championshipId"]').select('1');
        cy.get('input[name="name"]').type('Compétition Test');
        cy.get('textarea[name="description"]').type('Description de test');

        cy.get('input[name="start"]').type('2025-12-01');
        cy.get('input[name="end"]').type('2025-10-01');

        cy.get('button[type="submit"]').click();

        cy.get('.alert')
            .should('be.visible')
            .and('contain', 'La date de fin doit être strictement après la date de début');
    });

    it('devrait créer une compétition avec succès et rediriger', () => {
        cy.get('select[name="championshipId"]').select('1');
        cy.get('input[name="name"]').type('Ma Super Compétition');
        cy.get('textarea[name="description"]').type('Description de la compétition');
        cy.get('input[name="start"]').type('2025-12-01');
        cy.get('input[name="end"]').type('2026-12-01');

        cy.get('button[type="submit"]').click();

        // Vérification de l'appel API
        cy.wait('@createComp').then((interception) => {
            expect(interception.request.body).to.include({
                name: 'Ma Super Compétition',
                championshipId: 1 // Cypress envoie les valeurs de select comme string par défaut
            });
        });

        cy.get('.alert-success').should('contain', 'Compétition planifiée avec succès !');

        cy.url().should('include', '/admin');
    });
});