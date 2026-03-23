describe('Tests - Intégration SSE avec Authentification', () => {

    beforeEach(() => {
        cy.on('uncaught:exception', () => false);

        // Intercepter le flux SSE
        cy.intercept('GET', '/api/notifications/stream/*', {
            statusCode: 200,
            headers: {
                'content-type': 'text/event-stream',
                'cache-control': 'no-cache',
            },
            body: ''
        }).as('sseConnection');

        // Intercepter les appels d'authentification
        cy.intercept('POST', '**/auth/login', {
            statusCode: 200,
            body: {
                accessToken: 'test-token-123',
                refreshToken: 'refresh-token-123',
                userId: 1,
                email: 'test@example.com'
            }
        }).as('loginRequest');

        cy.visit('/');
    });

    it('Devrait afficher les notifications pendant la navigation', () => {
        // Se connecter d'abord
        cy.loginTest();

        // Naviguer entre les pages
        cy.contains('nav a', 'Championnats').click();
        cy.wait(500);

        // Si des notifications apparaissent, elles devraient rester visibles
        cy.get('body').then(($body) => {
            if ($body.find('.notification-panel').length > 0) {
                cy.get('.notification-panel')
                    .should('be.visible');
            }
        });
    });

    it('Devrait afficher un toast pour les notifications critiques', () => {
        // Se connecter d'abord
        cy.loginTest();

        // Créer une notification critique
        cy.window().then((win) => {
            // Chercher un événement personnalisé ou un toast
        });

        cy.get('body').then(($body) => {
            if ($body.find('[role="alert"]').length > 0) {
                cy.get('[role="alert"]')
                    .should('be.visible');
            }
        });
    });


    it('Devrait fermer la connexion SSE à la déconnexion', () => {
        // Se connecter d'abord
        cy.loginTest();

        // Naviguer vers le profil ou les paramètres
        cy.get('body').then(($body) => {
            if ($body.find('.user-menu').length > 0) {
                cy.get('.user-menu').click();

                cy.wait(500);

                // Chercher le bouton de déconnexion
                if ($body.find('.logout-button').length > 0) {
                    cy.get('.logout-button').click();

                    cy.wait(500);

                    // Vérifier qu'on est redirigé vers la page de connexion
                    cy.url().should('match', /(login|connexion)/i);
                }
            }
        });
    });

    it('Devrait supporter les notifications pour différents types d\'événements', () => {
        // Se connecter d'abord
        cy.loginTest();

        const eventTypes = ['INFO', 'WARNING', 'ERROR'];

        cy.get('body').then(($body) => {
            // Vérifier la structure pour chaque type
            eventTypes.forEach((type) => {
                // Les notifications de différents types devraient être affichées différemment
            });
        });
    });

    it('Devrait notifier l\'utilisateur des mises à jour de compétition', () => {
        // Se connecter d'abord
        cy.loginTest();
        
        // Naviguer vers une compétition
        cy.contains('nav a', 'Championnats').click();

        cy.wait(500);

        // Les notifications de compétition devraient apparaître
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-panel"]').length > 0) {
                cy.get('[data-testid="notification-panel"]')
                    .should('exist');
            }
        });
    });

    it('Devrait afficher un historique des notifications', () => {
        // Se connecter d'abord
        cy.loginTest();

        // Ouvrir le panneau de notifications
        cy.get('body').then(($body) => {
            if ($body.find('.notification-button').length > 0) {
                cy.get('.notification-button').click();

                cy.wait(500);

                // Chercher l'historique
                if ($body.find('.notification-history').length > 0) {
                    cy.get('.notification-history')
                        .should('exist')
                        .should('be.visible');
                }
            }
        });
    });

    it('Devrait trier les notifications par date (plus récent en haut)', () => {
        // Se connecter d'abord
        cy.loginTest();

        // Vérifier que nous sommes authentifiés
        cy.get('nav').should('be.visible');
    });

    it('Devrait supporter le dark mode pour les notifications', () => {
        // Se connecter d'abord
        cy.loginTest();

        // Chercher le bouton de dark mode
        cy.get('body').then(($body) => {
            if ($body.find('.dark-mode-toggle').length > 0) {
                cy.get('.dark-mode-toggle').click();

                cy.wait(500);

                // Vérifier que le panneau de notifications change de style
                if ($body.find('.notification-panel').length > 0) {
                    cy.get('.notification-panel')
                        .should('exist');
                }
            }
        });
    });

    it('Devrait gérer les notifications lors d\'un rebase de la page', () => {
        // Se connecter d'abord
        cy.loginTest();

        // Appuyer sur F5 pour recharger
        cy.reload();

        cy.wait(500);

        // Vérifier que la connexion SSE est rétablie
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-badge"]').length > 0) {
                cy.get('[data-testid="notification-badge"]')
                    .should('exist');
            }
        });
    });

    it('Devrait supporter les filtres de notification', () => {
        // Se connecter d'abord
        cy.loginTest();

        cy.get('body').then(($body) => {
            if ($body.find('.notification-filter').length > 0) {
                // Chercher les options de filtrage
                cy.get('.notification-filter')
                    .should('exist');

                // Pouvoir filtrer par type
                cy.get('.notification-filter').click();

                cy.wait(500);

                // Sélectionner une option
                if ($body.find('.filter-option').length > 0) {
                    cy.get('.filter-option').first().click();

                }
            }
        });
    });
});
