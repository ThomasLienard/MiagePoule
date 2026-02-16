describe('Page Profil - Signer la Charte', () => {

    beforeEach(() => {
        cy.on('uncaught:exception', () => false);
    });

    it('doit permettre à un athlète réel de se connecter et signer la charte', () => {
        cy.visit('/login');

        cy.get('input[placeholder="Email"]').type('athlete@test.com');
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.get('button[type="submit"]').click();

        cy.url().should('include', '/account');

        cy.get('.row').should('have.class', 'align-items-start');

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
        cy.get('input[placeholder="Email"]').type('commissaire@test.com');
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.get('button[type="submit"]').click();

        cy.wait(3000);
        cy.visit('/account');
        cy.wait(3000);
        // La carte de la charte ne doit pas être générée pour ce rôle
        cy.contains('Charte Européenne du Sport').should('not.exist');
    });
});