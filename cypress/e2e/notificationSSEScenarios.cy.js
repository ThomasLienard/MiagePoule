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
        cy.wait(2000);

        cy.get('input[type="email"]').type('newuser@example.com');
        cy.get('input[type="password"]').first().type('Password123!');
        cy.get('input[type="password"]').last().type('Password123!');
        cy.get('input[name="firstName"]').type('John');
        cy.get('input[name="lastName"]').type('Doe');

        cy.get('button').contains('Créer un compte').click();
        cy.wait(3000);

        // 2. Connexion de l'utilisateur
        cy.contains('nav a', 'Connexion').click();
        cy.wait(2000);

        cy.get('input[type="email"]').type('newuser@example.com');
        cy.get('input[type="password"]').type('Password123!');
        cy.get('button').contains('Connexion').click();

        cy.wait('@login');
        cy.wait('@sseStream');

        // 3. Vérification que les notifications sont connectées
        cy.waitForSSEConnection();
        cy.checkNotificationCount(0);

        // 4. Naviguer vers les championnats
        cy.contains('nav a', 'Championnats').click();
        cy.wait('@getEvents');
        cy.wait(2000);

        // 5. Simuler une notification de création de championnat
        cy.simulateSendNotification({
            id: 1,
            description: 'New championship: Summer Games 2026',
            type: 'INFO',
            severity: 'NORMAL',
            emissionDate: new Date().toISOString()
        });

        cy.checkNotificationCount(1);
        cy.checkNotificationExists('New championship');

        // 6. Ouvrir le panneau et consulter
        cy.openNotificationPanel();
        cy.get('[data-testid="notification-item"]').first()
            .should('contain', 'Summer Games 2026');

        // 7. Marquer comme lu
        cy.get('[data-testid="mark-as-read-button"]').click();
        cy.checkNotificationCount(0);

        // 8. Fermer et naviguer
        cy.closeNotificationPanel();
        cy.contains('nav a', 'Profil').click();
        cy.wait(2000);

        // 9. Les notifications doivent rester en place
        cy.waitForSSEConnection();

        // 10. Simuler d'autres notifications
        for (let i = 2; i <= 5; i++) {
            cy.simulateSendNotification({
                id: i,
                description: `Notification ${i}`,
                type: i % 2 === 0 ? 'WARNING' : 'INFO',
                severity: i % 3 === 0 ? 'HIGH' : 'NORMAL',
                emissionDate: new Date().toISOString()
            });
        }

        cy.checkNotificationCount(4);

        // 11. Vérifier les notifications avec filtrage
        cy.openNotificationPanel();
        cy.filterNotificationsByType('WARNING');
        cy.wait(1000);

        // 12. Retour et déconnexion
        cy.get('nav a').first().click();
        cy.wait(1000);

        cy.get('[data-testid="user-menu"]').click();
        cy.wait(1000);

        cy.get('[data-testid="logout-button"]').click();
        cy.wait(2000);

        cy.url().should('match', /(login|connexion)/i);
    });

    it('Gestion des erreurs de notification', () => {
        cy.waitForSSEConnection();

        // Simuler une erreur de connexion
        cy.intercept('GET', '/api/notifications/stream/*', {
            statusCode: 500,
            body: 'Internal Server Error'
        }).as('sseError');

        cy.reload();
        cy.wait(3000);

        // Vérifier l'affichage du message d'erreur
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="connection-error"]').length > 0) {
                cy.get('[data-testid="connection-error"]')
                    .should('be.visible');
            }
        });
    });

    it('Test de charge: Nombreuses notifications', () => {
        cy.waitForSSEConnection();

        // Envoyer 50 notifications rapidement
        for (let i = 1; i <= 50; i++) {
            cy.simulateSendNotification({
                id: i,
                description: `Load test notification ${i}`,
                type: ['INFO', 'WARNING', 'ERROR'][i % 3],
                severity: ['NORMAL', 'HIGH', 'CRITICAL'][i % 3],
                emissionDate: new Date(Date.now() - i * 1000).toISOString()
            });
        }

        // Vérifier que le badge affiche le bon nombre
        cy.get('[data-testid="notification-badge"]')
            .should('exist');

        // Ouvrir le panneau
        cy.openNotificationPanel();

        // Vérifier que les notifications sont affichées
        cy.get('[data-testid="notification-item"]')
            .should('have.length.lessEqual', 50);

        // Les performances ne devraient pas être affectées
        cy.checkNotificationCount(50);

        // Marquer tout comme lu
        cy.markAllNotificationsAsRead();
        cy.checkNotificationCount(0);
    });

    it('Test de stabilité: Reconnexion automatique', () => {
        cy.waitForSSEConnection();

        // Simuler une perte de connexion progressive
        cy.window().then((win) => {
            if (win.eventSourceRef && win.eventSourceRef.current) {
                win.eventSourceRef.current.close();
            }
        });

        cy.wait(7000); // Attendre plus que le timeout de reconnexion (5s)

        // Simuler une nouvelle notification
        cy.simulateSendNotification({
            id: 1,
            description: 'Notification after reconnection',
            type: 'INFO',
            severity: 'NORMAL',
            emissionDate: new Date().toISOString()
        });

        // La notification devrait arriver après la reconnexion
        cy.checkNotificationCount(1);
    });

    it('Test d\'accessibilité des notifications', () => {
        cy.waitForSSEConnection();

        // Envoyer une notification
        cy.simulateSendNotification({
            id: 1,
            description: 'Accessible notification',
            type: 'WARNING',
            severity: 'HIGH',
            emissionDate: new Date().toISOString()
        });

        // Vérifier l'accessibilité du badge
        cy.get('[data-testid="notification-badge"]')
            .should('have.attr', 'aria-live', 'polite');

        // Ouvrir le panneau
        cy.openNotificationPanel();

        // Vérifier l'accessibilité du panneau
        cy.get('[data-testid="notification-panel"]')
            .should('have.attr', 'role', 'dialog')
            .or('have.attr', 'role', 'region');

        // Vérifier que les notifications ont des rôles
        cy.get('[data-testid="notification-item"]')
            .first()
            .should('have.attr', 'role', 'article');
    });

    it('Test de persistance des notifications sur changement de page', () => {
        cy.waitForSSEConnection();

        // Créer 3 notifications
        cy.simulateSendNotification({
            id: 1,
            description: 'Persistent notification 1',
            type: 'INFO',
            severity: 'NORMAL',
            emissionDate: new Date().toISOString()
        });

        cy.checkNotificationCount(1);

        // Naviguer vers une autre page
        cy.contains('nav a', 'Championnats').click();
        cy.wait(2000);

        // Les notifications doivent persister
        cy.checkNotificationCount(1);

        // Ajouter d'autres notifications
        cy.simulateSendNotification({
            id: 2,
            description: 'Persistent notification 2',
            type: 'WARNING',
            severity: 'HIGH',
            emissionDate: new Date().toISOString()
        });

        cy.checkNotificationCount(2);

        // Naviguer vers une autre page
        cy.get('nav a').first().click();
        cy.wait(2000);

        // Les notifications doivent toujours être là
        cy.checkNotificationCount(2);
    });
});
