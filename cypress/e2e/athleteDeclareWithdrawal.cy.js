describe("Tests - Déclaration de forfait par un sportif", () => {

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
        cy.get('input[type="email"]').type('athlete@example.com');
        cy.wait(500);
        cy.get('input[type="password"]').type('test123');
        cy.wait(500);

        // Soumettre le formulaire
        cy.get('button[type="submit"]').click();
        cy.wait(3000);

        // Accéder à la page des épreuves du sportif
        cy.contains('Mes épreuves').click();
        cy.wait(2000);
    });

    it('Scénario : Un sportif peut déclarer forfait à une épreuve future', () => {
        // Vérifier que la page affiche les épreuves
        cy.contains('A venir').should('be.visible');
        
        // Cliquer sur le premier bouton "Déclarer forfait"
        cy.contains('button', 'Déclarer forfait').first().click();
        cy.wait(1000);
        
        // Vérifier que la modal de confirmation s'affiche
        cy.get('.modal').should('be.visible');
        cy.contains('Confirmer le forfait').should('be.visible');
        cy.wait(500);
        
        // Cliquer sur "Confirmer le forfait"
        cy.get('.modal').within(() => {
            cy.contains('button', 'Confirmer le forfait').click();
        });
        cy.wait(2000);
        
        // Vérifier que la modal se ferme et un message de succès apparaît
        cy.get('.modal').should('not.exist');
        cy.contains('Forfait déclaré avec succès').should('be.visible');
        cy.wait(1000);
    });

    it('Scénario : Un sportif ne peut pas déclarer forfait deux fois', () => {
        // Premier forfait
        cy.contains('button', 'Déclarer forfait').first().click();
        cy.wait(1000);
        
        cy.get('.modal').within(() => {
            cy.contains('button', 'Confirmer le forfait').click();
        });
        cy.wait(2000);
        
        // Essayer de déclarer forfait à nouveau (l'épreuve devrait ne plus afficher le bouton ou afficher une erreur)
        cy.contains('button', 'Déclarer forfait').first().click();
        cy.wait(1000);
        
        cy.get('.modal').within(() => {
            cy.contains('button', 'Confirmer le forfait').click();
        });
        cy.wait(2000);
        
        // Vérifier qu'une erreur apparaît
        cy.contains('déjà déclaré forfait').should('be.visible');
    });

    it('Scénario : Le modal de forfait peut être annulé', () => {
        // Cliquer sur le bouton "Déclarer forfait"
        cy.contains('button', 'Déclarer forfait').first().click();
        cy.wait(1000);
        
        // Vérifier que la modal s'affiche
        cy.get('.modal').should('be.visible');
        
        // Cliquer sur "Annuler"
        cy.get('.modal').within(() => {
            cy.contains('button', 'Annuler').click();
        });
        cy.wait(500);
        
        // Vérifier que la modal se ferme sans déclarer forfait
        cy.get('.modal').should('not.exist');
        cy.contains('Forfait déclaré avec succès').should('not.exist');
    });

    it('Scénario : Les épreuves passées ne devraient pas avoir de bouton forfait', () => {
        // Vérifier la section "Passés"
        cy.contains('Passés').should('be.visible');
        
        // Vérifier qu'aucun bouton "Déclarer forfait" n'apparaît dans la section "Passés"
        cy.contains('Passés').parent().within(() => {
            cy.contains('button', 'Déclarer forfait').should('not.exist');
        });
    });
});
