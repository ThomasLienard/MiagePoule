describe('Tests - Profil et Confidentialité', () => {

    beforeEach(() => {
        cy.on('uncaught:exception', () => false);
        
        // Étape de connexion préalable
        cy.visit('/login');
        cy.get('input[placeholder="Email"]').type('anna@example.com');
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.get('button[type="submit"]').click();
        cy.wait(500);
    });

    it('Scénario : Mise à jour des informations du profil', () => {
        cy.visit('/account');
        cy.wait(500);

        // 1. Passer en mode édition
        cy.contains('button', 'Modifier le profil').click();

        // 2. Modifier le prénom
        // On cherche le label "Prénom" puis l'input qui suit juste après
        cy.contains('label', 'Prénom').next('input').clear().type('Jean');

        // 3. Modifier le nom
        cy.contains('label', 'Nom').next('input').clear().type('Dupont');

        // 4. Sélection du pays (Form.Select)
        cy.get('select').select(1); // Sélectionne la deuxième option de la liste

        // 5. Sauvegarder
        cy.contains('button', 'Sauvegarder').click();
        cy.wait(500);

        // Vérification du message de succès
        cy.get('.alert-success').should('be.visible');
    });

    it('Scénario : Changement de mot de passe via la modale', () => {
        cy.visit('/account');

        // 1. Ouvrir la modale
        cy.contains('button', 'mot de passe').click();

        // On s'assure que la modale est bien visible avant de taper
        cy.get('.modal-dialog').should('be.visible');

        cy.contains('label', 'Mot de passe actuel').next('input').type('test123');
        cy.contains('label', 'Nouveau mot de passe').next('input').type('newPassword456!');

        // 3. Valider
        cy.get('.modal-footer').contains('Mettre à jour').click();

        // Vérifier le succès
        cy.get('.modal-body .alert-success', { timeout: 10000 })
            .should('be.visible')
            .and('contain', 'Mot de passe modifié avec succès');

        cy.get('.modal-dialog').should('not.exist');

        // --- REMETTRE LE MOT DE PASSE INITIAL ---
        cy.contains('button', 'mot de passe').click();
        cy.get('.modal-dialog').should('be.visible');

        cy.contains('label', 'Mot de passe actuel').next('input').type('newPassword456!');
        cy.contains('label', 'Nouveau mot de passe').next('input').type('test123');

        cy.get('.modal-footer').contains('Mettre à jour').click();
        cy.get('.modal-body .alert-success').should('be.visible');
    });
});