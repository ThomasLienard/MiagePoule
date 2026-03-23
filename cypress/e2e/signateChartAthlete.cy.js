describe('Page Profil - Signer la Charte', () => {

    beforeEach(() => {
        cy.on('uncaught:exception', () => false);
    });

    //remettre à faux dans la base pour l'athlète concerné sinon le test plante car on ne peut pas redo cette action
    it('doit permettre à un athlète réel de se connecter et signer la charte', () => {
        cy.visit('/login');

        cy.get('input[placeholder="Email"]').type('arnaud@example.com');
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.get('button[type="submit"]').click();

        cy.url().should('include', '/account');

        // On vérifie que la carte est là (seulement pour l'athlète)
        cy.contains('Charte Européenne du Sport').should('be.visible');

        cy.get('#check-charte').should('be.disabled');

        cy.get('.accordion-header').click();

        cy.contains('Article 8 – Intégrité du sport').should('be.visible');

        cy.get('a')
            .should('have.attr', 'href', '/')
            .and('be.visible');

        cy.get('#check-charte').should('not.be.disabled').check();

        cy.contains('Charte signée avec succès').should('be.visible');

        cy.get('#check-charte').should('be.disabled');
    });

    it('ne doit pas afficher la charte pour un commissaire réel', () => {
        cy.visit('/login');
        cy.get('input[placeholder="Email"]').type('commissaire@example.com');
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.get('button[type="submit"]').click();

        cy.wait(3000);
        cy.visit('/account');
        cy.wait(3000);
        // La carte de la charte ne doit pas être générée pour ce rôle
        cy.contains('Charte Européenne du Sport').should('not.exist');
    });
});