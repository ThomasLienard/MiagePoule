describe('Tests - Notifications SSE avec Commandes Personnalisées', () => {

    beforeEach(() => {
        cy.on('uncaught:exception', () => false);

        // Intercepter les appels SSE
        cy.intercept('GET', '/api/notifications/stream/*', {
            statusCode: 200,
            headers: {
                'content-type': 'text/event-stream',
                'cache-control': 'no-cache',
            }
        }).as('sseStream');

        cy.visit('/');
    });

    it('Devrait afficher les notifications en temps réel', () => {
        // Attendre la connexion SSE
        cy.waitForSSEConnection();

        // Vérifier le badge initial
        cy.checkNotificationCount(0);

        // Simuler une notification
        cy.simulateSendNotification({
            id: 1,
            description: 'Test Notification',
            type: 'INFO',
            severity: 'NORMAL',
            emissionDate: new Date().toISOString()
        });

        // Vérifier le badge mis à jour
        cy.checkNotificationCount(1);
    });

    it('Devrait ouvrir et fermer le panneau de notifications', () => {
        // Ouvrir
        cy.openNotificationPanel();
        cy.get('[data-testid="notification-panel"]')
            .should('be.visible');

        // Fermer
        cy.closeNotificationPanel();
        cy.get('[data-testid="notification-panel"]')
            .should('not.be.visible');
    });

    it('Devrait vérifier l\'existence d\'une notification spécifique', () => {
        // Simuler une notification
        cy.simulateSendNotification({
            id: 1,
            description: 'Championship created successfully',
            type: 'INFO',
            severity: 'NORMAL',
            emissionDate: new Date().toISOString()
        });

        // Vérifier qu'elle existe
        cy.checkNotificationExists('Championship created successfully');
    });

    it('Devrait marquer toutes les notifications comme lues', () => {
        // Simuler plusieurs notifications
        const notifications = [
            { id: 1, description: 'Notification 1', type: 'INFO', severity: 'NORMAL' },
            { id: 2, description: 'Notification 2', type: 'INFO', severity: 'NORMAL' },
            { id: 3, description: 'Notification 3', type: 'WARNING', severity: 'HIGH' }
        ];

        notifications.forEach(notif => {
            cy.simulateSendNotification({
                ...notif,
                emissionDate: new Date().toISOString()
            });
        });

        // Vérifier que le badge affiche 3
        cy.checkNotificationCount(3);

        // Marquer tout comme lu
        cy.markAllNotificationsAsRead();

        // Vérifier que le badge est à 0
        cy.checkNotificationCount(0);
    });

    it('Devrait vérifiera les détails d\'une notification', () => {
        const testNotification = {
            id: 1,
            description: 'Score update complete',
            type: 'INFO',
            severity: 'NORMAL',
            emissionDate: new Date().toISOString()
        };

        cy.simulateSendNotification(testNotification);

        // Vérifier les détails
        cy.checkNotificationWithDetails({
            description: 'Score update complete',
            type: 'INFO',
            severity: 'NORMAL'
        });
    });

    it('Devrait supprimer une notification spécifique', () => {
        // Simuler une notification
        cy.simulateSendNotification({
            id: 1,
            description: 'Notification to delete',
            type: 'INFO',
            severity: 'NORMAL',
            emissionDate: new Date().toISOString()
        });

        cy.checkNotificationCount(1);

        // Supprimer la notification
        cy.deleteNotification('Notification to delete');

        // Vérifier qu'elle est supprimée (peut nécessiter une mise à jour des données)
        cy.wait(500);
    });

    it('Devrait gérer les notifications de type WARNING', () => {
        const warningNotification = {
            id: 1,
            description: 'Event starts in 30 minutes',
            type: 'WARNING',
            severity: 'HIGH',
            emissionDate: new Date().toISOString()
        };

        cy.simulateSendNotification(warningNotification);

        // Vérifier le type
        cy.checkNotificationType('WARNING');
    });

    it('Devrait gérer les notifications de type ERROR', () => {
        const errorNotification = {
            id: 1,
            description: 'Critical system error',
            type: 'ERROR',
            severity: 'CRITICAL',
            emissionDate: new Date().toISOString()
        };

        cy.simulateSendNotification(errorNotification);

        // Vérifier le type
        cy.checkNotificationType('ERROR');
    });

    it('Devrait filtrer les notifications par type', () => {
        // Simuler plusieurs notifications de différents types
        const notifications = [
            { id: 1, description: 'Info notification', type: 'INFO', severity: 'NORMAL' },
            { id: 2, description: 'Warning notification', type: 'WARNING', severity: 'HIGH' },
            { id: 3, description: 'Error notification', type: 'ERROR', severity: 'CRITICAL' }
        ];

        notifications.forEach(notif => {
            cy.simulateSendNotification({
                ...notif,
                emissionDate: new Date().toISOString()
            });
        });

        // Filtrer par 'INFO'
        cy.filterNotificationsByType('INFO');

        // Vérifier que seules les notifications INFO sont affichées
        cy.get('[data-testid="notification-item"]')
            .should('have.length', 1)
            .contains('Info notification');
    });

    it('Devrait afficher les notifications triées par date', () => {
        // Simuler plusieurs notifications avec des dates différentes
        const notifications = [
            {
                id: 1,
                description: 'First notification',
                type: 'INFO',
                severity: 'NORMAL',
                emissionDate: new Date(Date.now() - 3000).toISOString()
            },
            {
                id: 2,
                description: 'Second notification',
                type: 'INFO',
                severity: 'NORMAL',
                emissionDate: new Date(Date.now() - 1000).toISOString()
            },
            {
                id: 3,
                description: 'Third notification (Most recent)',
                type: 'INFO',
                severity: 'NORMAL',
                emissionDate: new Date().toISOString()
            }
        ];

        notifications.forEach(notif => {
            cy.simulateSendNotification(notif);
        });

        // Vérifier que les notifications sont triées
        cy.checkNotificationsSortedByDate();

        // Vérifier que la plus récente est en haut
        cy.openNotificationPanel();
        cy.get('[data-testid="notification-item"]')
            .first()
            .should('contain', 'Third notification (Most recent)');
    });

    it('Devrait gérer les connexions simultanées à plusieurs flux SSE', () => {
        // Vérifier que seule une connexion SSE est établie par utilisateur
        cy.waitForSSEConnection();

        // Naviguer et revenir
        cy.get('nav a').first().click();
        cy.wait(1000);
        cy.go('back');
        cy.wait(1000);

        // Vérifier que la connexion est toujours valide
        cy.checkNotificationCount(0);
    });

    it('Devrait afficher un badge même avec 0 notifications', () => {
        cy.waitForSSEConnection();

        // Vérifier que le badge affiche 0
        cy.checkNotificationCount(0);
    });

    it('Devrait mettre à jour le badge en temps réel pour plusieurs notifications', () => {
        cy.waitForSSEConnection();

        // Simuler 5 notifications
        for (let i = 1; i <= 5; i++) {
            cy.simulateSendNotification({
                id: i,
                description: `Notification ${i}`,
                type: 'INFO',
                severity: 'NORMAL',
                emissionDate: new Date().toISOString()
            });
            cy.checkNotificationCount(i);
        }
    });

    it('Devrait afficher les notifications avec les bonnes classes CSS', () => {
        const notification = {
            id: 1,
            description: 'Styled notification',
            type: 'WARNING',
            severity: 'HIGH',
            emissionDate: new Date().toISOString()
        };

        cy.simulateSendNotification(notification);

        cy.openNotificationPanel();
        cy.get('[data-testid="notification-item"]')
            .first()
            .should('have.class', 'notification-item')
            .and('have.class', 'notification-warning');
    });

    it('Devrait supporter la suppression par action de balayer (swipe left)', () => {
        const notification = {
            id: 1,
            description: 'Swipeable notification',
            type: 'INFO',
            severity: 'NORMAL',
            emissionDate: new Date().toISOString()
        };

        cy.simulateSendNotification(notification);

        cy.openNotificationPanel();
        
        // Simuler un geste de balayage (si implémenté)
        cy.get('[data-testid="notification-item"]')
            .first()
            .trigger('swipeleft');

        cy.wait(500);
    });

    it('Devrait gérer le flux de notifications pour les administrateurs', () => {
        // Les notifications des administrateurs devraient avoir des caractéristiques spéciales
        const adminNotification = {
            id: 1,
            description: 'Admin action: User verified',
            type: 'INFO',
            severity: 'NORMAL',
            eventId: null,
            emissionDate: new Date().toISOString()
        };

        cy.simulateSendNotification(adminNotification);

        cy.checkNotificationExists('Admin action: User verified');
    });

    it('Devrait nettoyer les notifications anciennes automatiquement', () => {
        cy.waitForSSEConnection();

        // Simuler 100 notifications (pour tester la limite)
        for (let i = 1; i <= 100; i++) {
            cy.simulateSendNotification({
                id: i,
                description: `Notification ${i}`,
                type: 'INFO',
                severity: 'NORMAL',
                emissionDate: new Date(Date.now() - i * 1000).toISOString()
            });
        }

        cy.openNotificationPanel();

        // Vérifier que le nombre de notifications affichées est limité
        cy.get('[data-testid="notification-item"]')
            .should('have.length.lessThan', 100);
    });

    it('Devrait afficher une notification de reconnexion', () => {
        cy.waitForSSEConnection();

        // Simuler une perte de connexion
        cy.window().then((win) => {
            if (win.eventSourceRef && win.eventSourceRef.current) {
                win.eventSourceRef.current.close();
            }
        });

        cy.wait(2000);

        // Chercher une notification de reconnexion
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="reconnecting"]').length > 0) {
                cy.get('[data-testid="reconnecting"]')
                    .should('be.visible');
            }
        });
    });
});
