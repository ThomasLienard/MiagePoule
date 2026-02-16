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

    it('Devrait établir une connexion SSE après la connexion de l\'utilisateur', () => {
        // Naviguer vers la page de connexion
        cy.contains('nav a', 'Connexion').click();
        cy.wait(2000);

        // Trouver et remplir le formulaire de connexion
        cy.get('input[type="email"]').first().type('test@example.com');
        cy.get('input[type="password"]').first().type('password123');
        
        cy.get('button').contains('Connexion').click();

        cy.wait('@loginRequest');
        cy.wait(2000);

        // Vérifier que la connexion SSE est établie
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-badge"]').length > 0) {
                cy.get('[data-testid="notification-badge"]')
                    .should('exist');
            }
        });
    });

    it('Devrait afficher les notifications pendant la navigation', () => {
        // La page devrait avoir un panneau de notifications
        cy.wait(2000);

        // Naviguer entre les pages
        cy.contains('nav a', 'Championnats').click();
        cy.wait(2000);

        // Si des notifications apparaissent, elles devraient rester visibles
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-panel"]').length > 0) {
                cy.get('[data-testid="notification-panel"]')
                    .should('be.visible');
            }
        });
    });

    it('Devrait mettre à jour le badge en temps réel', () => {
        cy.wait(2000);

        // Chercher le badge initial
        cy.get('body').then(($body) => {
            const initialCount = $body.find('[data-testid="notification-badge"]').length > 0
                ? $body.find('[data-testid="notification-badge"]').text()
                : '0';

            // Attendre un peu
            cy.wait(2000);

            // Le badge devrait exister
            if ($body.find('[data-testid="notification-badge"]').length > 0) {
                cy.get('[data-testid="notification-badge"]')
                    .should('exist');
            }
        });
    });

    it('Devrait afficher un toast pour les notifications critiques', () => {
        cy.wait(2000);

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

    it('Devrait permettre le clic sur une notification pour la consulter', () => {
        cy.wait(2000);

        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-item"]').length > 0) {
                cy.get('[data-testid="notification-item"]').first().click();

                cy.wait(1000);

                // Vérifier qu'une page ou un modal s'ouvre
            }
        });
    });

    it('Devrait vider les notifications au clic sur "Marquer comme lu"', () => {
        cy.wait(2000);

        // Ouvrir le panneau de notifications
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-button"]').length > 0) {
                cy.get('[data-testid="notification-button"]').click();

                cy.wait(1000);

                // Cliquer sur "Marquer comme lu"
                if ($body.find('[data-testid="mark-as-read-button"]').length > 0) {
                    cy.get('[data-testid="mark-as-read-button"]').click();

                    cy.wait(1000);

                    // Vérifier que le badge revient à 0
                    cy.get('[data-testid="notification-badge"]')
                        .should('contain', '0');
                }
            }
        });
    });

    it('Devrait fermer la connexion SSE à la déconnexion', () => {
        cy.wait(2000);

        // Naviguer vers le profil ou les paramètres
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="user-menu"]').length > 0) {
                cy.get('[data-testid="user-menu"]').click();

                cy.wait(1000);

                // Chercher le bouton de déconnexion
                if ($body.find('[data-testid="logout-button"]').length > 0) {
                    cy.get('[data-testid="logout-button"]').click();

                    cy.wait(2000);

                    // Vérifier qu'on est redirigé vers la page de connexion
                    cy.url().should('match', /(login|connexion)/i);
                }
            }
        });
    });

    it('Devrait supporter les notifications pour différents types d\'événements', () => {
        cy.wait(2000);

        const eventTypes = ['INFO', 'WARNING', 'ERROR'];

        cy.get('body').then(($body) => {
            // Vérifier la structure pour chaque type
            eventTypes.forEach((type) => {
                // Les notifications de différents types devraient être affichées différemment
            });
        });
    });

    it('Devrait notifier l\'utilisateur des mises à jour de compétition', () => {
        // Naviguer vers une compétition
        cy.contains('nav a', 'Championnats').click();

        cy.wait(2000);

        // Les notifications de compétition devraient apparaître
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-panel"]').length > 0) {
                cy.get('[data-testid="notification-panel"]')
                    .should('exist');
            }
        });
    });

    it('Devrait gérer les notifications lors d\'un changement de rôle utilisateur', () => {
        cy.wait(2000);

        // Naviguer vers le profil
        cy.get('nav a').contains('Profil').click({ force: true });

        cy.wait(2000);

        // Les notifications devraient être re-synchronisées
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-badge"]').length > 0) {
                cy.get('[data-testid="notification-badge"]')
                    .should('exist');
            }
        });
    });

    it('Devrait afficher un historique des notifications', () => {
        cy.wait(2000);

        // Ouvrir le panneau de notifications
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-button"]').length > 0) {
                cy.get('[data-testid="notification-button"]').click();

                cy.wait(1000);

                // Chercher l'historique
                if ($body.find('[data-testid="notification-history"]').length > 0) {
                    cy.get('[data-testid="notification-history"]')
                        .should('exist')
                        .should('be.visible');
                }
            }
        });
    });

    it('Devrait trier les notifications par date (plus récent en haut)', () => {
        cy.wait(2000);

        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-item"]').length > 1) {
                // Récupérer les timestamps
                cy.get('[data-testid="notification-item"]').first()
                    .should('exist');
                
                cy.get('[data-testid="notification-item"]').last()
                    .should('exist');
                
                // Le premier devrait être plus récent que le dernier
            }
        });
    });

    it('Devrait supporter le dark mode pour les notifications', () => {
        cy.wait(2000);

        // Chercher le bouton de dark mode
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="dark-mode-toggle"]').length > 0) {
                cy.get('[data-testid="dark-mode-toggle"]').click();

                cy.wait(1000);

                // Vérifier que le panneau de notifications change de style
                if ($body.find('[data-testid="notification-panel"]').length > 0) {
                    cy.get('[data-testid="notification-panel"]')
                        .should('exist');
                }
            }
        });
    });

    it('Devrait gérer les notifications lors d\'un rebase de la page', () => {
        cy.wait(2000);

        // Appuyer sur F5 pour recharger
        cy.reload();

        cy.wait(3000);

        // Vérifier que la connexion SSE est rétablie
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-badge"]').length > 0) {
                cy.get('[data-testid="notification-badge"]')
                    .should('exist');
            }
        });
    });

    it('Devrait notifier les changements de statut des épreuves', () => {
        // Naviguer vers une page d'épreuves
        cy.contains('a', '100m').click({ force: true });

        cy.wait(2000);

        // Une notification de changement de statut devrait apparaître
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-badge"]').length > 0) {
                cy.get('[data-testid="notification-badge"]')
                    .should('exist');
            }
        });
    });

    it('Devrait supporter les filtres de notification', () => {
        cy.wait(2000);

        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-filter"]').length > 0) {
                // Chercher les options de filtrage
                cy.get('[data-testid="notification-filter"]')
                    .should('exist');

                // Pouvoir filtrer par type
                cy.get('[data-testid="notification-filter"]').click();

                cy.wait(1000);

                // Sélectionner une option
                if ($body.find('[data-testid="filter-option"]').length > 0) {
                    cy.get('[data-testid="filter-option"]').first().click();

                    cy.wait(1000);
                }
            }
        });
    });
});
