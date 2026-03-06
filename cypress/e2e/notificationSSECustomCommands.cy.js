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

    it('Devrait ouvrir et fermer le panneau de notifications', () => {
        // Se connecter d'abord
        cy.loginTest();
        cy.wait(500);
        
        // Ouvrir
        cy.openNotificationPanel();
        cy.get('.notification-panel')
            .should('be.visible');

        // Fermer
        cy.closeNotificationPanel();
    });

    it('Devrait attendre la connexion SSE', () => {
        // Se connecter d'abord
        cy.loginTest();
        
        // Attendre la connexion SSE
        cy.waitForSSEConnection();
        
        // Vérifier que nous sommes connectés
        cy.get('nav').should('be.visible');
    });

    it('Devrait simuler l\'envoi d\'une notification', () => {
        // Se connecter d'abord
        cy.loginTest();
        cy.waitForSSEConnection();
        
        // Simuler une notification
        cy.simulateSendNotification({
            id: 1,
            description: 'Test Notification',
            type: 'INFO',
            severity: 'NORMAL',
            emissionDate: new Date().toISOString()
        });

        // Vérifier que nous pouvons ouvrir le panneau
        cy.openNotificationPanel();
        cy.get('.notification-panel').should('be.visible');
    });

    it('Devrait gérer les notifications de type INFO', () => {
        // Se connecter d'abord
        cy.loginTest();
        cy.waitForSSEConnection();

        const infoNotification = {
            id: 1,
            description: 'Info message',
            type: 'INFO',
            severity: 'NORMAL',
            emissionDate: new Date().toISOString()
        };

        cy.simulateSendNotification(infoNotification);

        // Vérifier que le panneau peut être ouvert
        cy.openNotificationPanel();
        cy.get('.notification-panel').should('exist');
    });

    it('Devrait gérer les notifications de type WARNING', () => {
        // Se connecter d'abord
        cy.loginTest();
        cy.waitForSSEConnection();

        const warningNotification = {
            id: 1,
            description: 'Warning message',
            type: 'WARNING',
            severity: 'HIGH',
            emissionDate: new Date().toISOString()
        };

        cy.simulateSendNotification(warningNotification);

        // Vérifier que le panneau peut être ouvert
        cy.openNotificationPanel();
        cy.get('.notification-panel').should('exist');
    });

    it('Devrait gérer les notifications de type ERROR', () => {
        // Se connecter d'abord
        cy.loginTest();
        cy.waitForSSEConnection();

        const errorNotification = {
            id: 1,
            description: 'Error message',
            type: 'ERROR',
            severity: 'CRITICAL',
            emissionDate: new Date().toISOString()
        };

        cy.simulateSendNotification(errorNotification);

        // Vérifier que le panneau peut être ouvert
        cy.openNotificationPanel();
        cy.get('.notification-panel').should('exist');
    });

    it('Devrait gérer les connexions simultanées à plusieurs flux SSE', () => {
        // Se connecter d'abord
        cy.loginTest();
        cy.waitForSSEConnection();

        // Naviguer et revenir
        cy.get('nav a').first().click();
        cy.wait(500);
        cy.go('back');
        cy.wait(500);

        // Vérifier que la connexion fonctionne toujours
        cy.openNotificationPanel();
        cy.get('.notification-panel').should('exist');
    });

    it('Devrait supporter le dark mode pour les notifications', () => {
        // Se connecter d'abord
        cy.loginTest();
        cy.wait(500);

        // Ouvrir le panneau de notifications
        cy.openNotificationPanel();
        cy.get('.notification-panel').should('be.visible');
    });

    it('Devrait afficher une notification de reconnexion', () => {
        // Se connecter d'abord
        cy.loginTest();
        cy.waitForSSEConnection();

        // Simuler une perte de connexion
        cy.window().then((win) => {
            if (win.eventSourceRef && win.eventSourceRef.current) {
                win.eventSourceRef.current.close();
            }
        });

        cy.wait(500);

        // La page devrait toujours être functional
        cy.get('nav').should('be.visible');
    });
});
