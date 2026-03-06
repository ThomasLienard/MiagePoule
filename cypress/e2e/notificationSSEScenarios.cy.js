/**
 * Exemple d'intégration des Tests de Notifications SSE
 * dans un scénario de test complet
 */

describe('Scénario Complet - Utilisateur avec Notifications', () => {

    beforeEach(() => {
        cy.on('uncaught:exception', () => false);

        // Setup des intercepteurs
        cy.intercept('GET', '/api/notifications/stream/*').as('sseStream');
        cy.intercept('POST', '**/auth/login').as('login');
        cy.intercept('GET', '**/events').as('getEvents');

        cy.visit('/');
    });

    it('Flux complet: Inscription -> Connexion -> Réception de notifications', () => {
        // 1. Inscription d'un nouvel utilisateur
        cy.contains('nav a', 'Inscription').click();
        cy.wait(500);

        cy.get('input[type="email"]').type('newuser@example.com');
        cy.get('input[type="password"]').first().type('Password123!');
        cy.get('input[type="password"]').last().type('Password123!');
        cy.get('input[name="firstName"]').type('John');
        cy.get('input[name="lastName"]').type('Doe');

        cy.get('button').contains('S\'inscrire').click();
        cy.wait(500);

        // 2. Connexion de l'utilisateur
        cy.contains('nav a', 'Connexion').click();
        cy.wait(500);

        cy.get('input[type="email"]').type('newuser@example.com');
        cy.get('input[type="password"]').type('Password123!');
        cy.get('button').contains('Se connecter').click();

        // 3. Vérification que les notifications sont connectées
        cy.waitForSSEConnection();

        // 4. Naviguer vers les championnats
        cy.contains('nav a', 'Championnats').click();
        cy.wait(500);

        // 5. Simuler une notification de création de championnat
        cy.simulateSendNotification({
            id: 1,
            description: 'New championship: Summer Games 2026',
            type: 'INFO',
            severity: 'NORMAL',
            emissionDate: new Date().toISOString()
        });

        // 6. Ouvrir le panneau et consulter
        cy.openNotificationPanel();

        // 8. Fermer et naviguer
        cy.closeNotificationPanel();
        cy.contains('nav a', 'Profil').click();
        cy.wait(500);

        
        // 9. Retour et déconnexion
        cy.get('nav a').first().click();
        cy.wait(500);

        cy.get('nav a').contains("Déconnexion").click();
        cy.wait(500);

        cy.url().should('match', /(login|connexion)/i);
    });


});
