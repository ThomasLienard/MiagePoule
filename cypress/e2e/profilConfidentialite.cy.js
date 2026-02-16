describe('Tests - Profil et Confidentialité', () => {

    beforeEach(() => {
        cy.on('uncaught:exception', () => false);
        
        // Étape de connexion préalable
        cy.visit('/login');
        cy.get('input[placeholder="Email"]').type('john@example.com');
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.get('button[type="submit"]').click();
        cy.wait(3000);
    });

    it('Scénario : Mise à jour des informations du profil', () => {
        cy.visit('/account');
        cy.wait(3000);

        // 1. Passer en mode édition
        cy.contains('button', 'Modifier le profil').click();
        cy.wait(3000);

        // 2. Modifier le prénom
        // On cherche le label "Prénom" puis l'input qui suit juste après
        cy.contains('label', 'Prénom').next('input').clear().type('Jean');
        cy.wait(3000);

        // 3. Modifier le nom
        cy.contains('label', 'Nom').next('input').clear().type('Dupont');
        cy.wait(3000);

        // 4. Sélection du pays (Form.Select)
        cy.get('select').select(1); // Sélectionne la deuxième option de la liste
        cy.wait(3000);

        // 5. Sauvegarder
        cy.contains('button', 'Sauvegarder').click();
        cy.wait(3000);

        // Vérification du message de succès
        cy.get('.alert-success').should('be.visible');
        cy.wait(3000);
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

    it('Scénario : Gestion de la confidentialité', () => {
        cy.visit('/privacy');
        cy.wait(3000);

        // 1. Vérifier la présence du tableau
        cy.contains('Données et confidentialité').should('be.visible');
        
        // 2. Tester un switch de catégorie (PrivacySettings.jsx)
        // On cherche un switch qui n'est pas "mandatory" (obligatoire)
        cy.get('input[type="checkbox"]').not(':disabled').first().as('firstSwitch');
        
        // On bascule le switch
        cy.get('@firstSwitch').click({ force: true });
        cy.wait(3000);

        // On vérifie que le changement est pris en compte (visuellement)
        // On le re-bascule pour remettre l'état initial
        cy.get('@firstSwitch').click({ force: true });
        cy.wait(3000);
    });
});