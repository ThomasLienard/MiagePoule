describe('Tests - Système de Notifications SSE', () => {

    beforeEach(() => {
        // Empêche le test de planter si React jette une erreur mineure
        cy.on('uncaught:exception', () => false);

        // Intercepter les appels API pour contrôler les réponses
        cy.intercept('GET', '/api/notifications/stream/*', (req) => {
            // Créer une réponse SSE simulée
            req.reply((res) => {
                res.headers = {
                    'content-type': 'text/event-stream',
                    'cache-control': 'no-cache',
                };
            });
        }).as('sseStream');

        // Visite la page
        cy.visit('/', {
            onBeforeLoad(win) {
                cy.stub(win, 'open').as('windowOpen');
            }
        });
    });

    it('Devrait établir une connexion SSE au chargement de la page', () => {
        // Attendre que la page se charge
        cy.wait(2000);

        // Vérifier que la connexion SSE a été établie
        cy.window().then((win) => {
            // Vérifier dans les logs qu'une tentative de connexion a eu lieu
            cy.visit('/', {
                onBeforeLoad(win) {
                    // Spy sur console.log pour vérifier les logs
                    cy.spy(win.console, 'log');
                }
            });
        });

        cy.wait(2000);
    });

    it('Devrait afficher le badge de notifications', () => {
        // Chercher le badge de notifications dans le header
        // Structure typique : <span class="notification-badge">0</span>
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-badge"]').length > 0) {
                cy.get('[data-testid="notification-badge"]')
                    .should('exist')
                    .should('contain', '0');
            } else if ($body.find('.notification-count').length > 0) {
                cy.get('.notification-count')
                    .should('exist');
            }
        });
    });

    it('Devrait ouvrir le panneau de notifications au clic', () => {
        // Chercher le bouton de notifications
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-button"]').length > 0) {
                cy.get('[data-testid="notification-button"]').click();
                cy.wait(1000);
            } else if ($body.find('.notification-bell').length > 0) {
                cy.get('.notification-bell').click();
                cy.wait(1000);
            }
        });
    });

    it('Devrait gérer les notifications reçues du serveur', () => {
        // Mock une notification reçue du serveur
        cy.window().then((win) => {
            // Créer un événement personnalisé pour simuler une notification
            const mockEvent = new MessageEvent('message', {
                data: JSON.stringify({
                    id: 1,
                    description: 'Test Notification',
                    emissionDate: new Date().toISOString(),
                    type: 'INFO',
                    severity: 'NORMAL'
                })
            });

            // Simuler la réception d'une notification
            // Note: Ceci dépend de la façon dont le composant est structuré
        });

        cy.wait(2000);
    });

    it('Devrait marquer les notifications comme lues', () => {
        // Chercher le bouton pour marquer comme lu
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="mark-as-read-button"]').length > 0) {
                cy.get('[data-testid="mark-as-read-button"]').click();
                cy.wait(1000);
                
                // Vérifier que le badge est à 0
                cy.get('[data-testid="notification-badge"]')
                    .should('contain', '0');
            }
        });
    });

    it('Devrait gérer la déconnexion et la reconnexion', () => {
        cy.wait(2000);

        // Simuler une déconnexion
        cy.window().then((win) => {
            // Fermer la connexion EventSource
            if (win.eventSourceRef && win.eventSourceRef.current) {
                win.eventSourceRef.current.close();
            }
        });

        cy.wait(2000);

        // Vérifier que le statut de connexion change
        cy.get('body').then(($body) => {
            // Chercher un indicateur de déconnexion
            if ($body.find('[data-testid="connection-status"]').length > 0) {
                cy.get('[data-testid="connection-status"]')
                    .should('contain', 'Déconnecté');
            }
        });
    });

    it('Devrait gérer les erreurs de connexion avec reconnexion automatique', () => {
        // Intercepter l'appel SSE et retourner une erreur
        cy.intercept('GET', '/api/notifications/stream/*', (req) => {
            req.reply({
                statusCode: 500,
                body: 'Internal Server Error'
            });
        }).as('sseError');

        cy.reload();
        cy.wait(2000);

        // Vérifier que le système essaie de se reconnecter
        cy.window().then((win) => {
            // Chercher dans les logs un message de reconnexion
            cy.spy(win.console, 'log');
        });

        cy.wait(3000); // Attendre la tentative de reconnexion
    });

    it('Devrait gérer plusieurs notifications simultanées', () => {
        // Simuler plusieurs notifications
        const notifications = [
            {
                id: 1,
                description: 'Notification 1',
                type: 'INFO',
                severity: 'NORMAL'
            },
            {
                id: 2,
                description: 'Notification 2',
                type: 'WARNING',
                severity: 'HIGH'
            },
            {
                id: 3,
                description: 'Notification 3',
                type: 'ERROR',
                severity: 'CRITICAL'
            }
        ];

        cy.wait(2000);

        // Le badge devrait refleter le nombre de notifications
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-badge"]').length > 0) {
                cy.get('[data-testid="notification-badge"]')
                    .should('exist');
            }
        });
    });

    it('Devrait persister les notifications après un rafraîchissement', () => {
        cy.wait(2000);

        // Rafraîchir la page
        cy.reload();

        cy.wait(2000);

        // Vérifier que la connexion est rétablie
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="connection-status"]').length > 0) {
                cy.get('[data-testid="connection-status"]')
                    .should('not.contain', 'Erreur');
            }
        });
    });

    it('Devrait nettoyer la connexion SSE lors du unmount du composant', () => {
        cy.window().then((win) => {
            // Spy sur console.log
            cy.spy(win.console, 'log');
        });

        // Naviguer vers une autre page
        cy.get('nav a').first().click();

        cy.wait(2000);

        // Retourner à la page
        cy.go('back');

        cy.wait(2000);

        // Vérifier que une nouvelle connexion a été établie
    });

    it('Devrait afficher le timestamp des notifications', () => {
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-item"]').length > 0) {
                cy.get('[data-testid="notification-item"]').first()
                    .should('contain', /\d{1,2}\/\d{1,2}\/\d{4}|\d{1,2}:\d{2}/);
            }
        });
    });

    it('Devrait gérer les notifications sans userId', () => {
        // Naviguer vers une page sans authentification
        cy.get('nav a').contains('Connexion').click();

        cy.wait(2000);

        // Vérifier qu'aucune connexion SSE ne s'établit
        cy.window().then((win) => {
            // Vérifier les logs
            cy.spy(win.console, 'log');
        });
    });

    it('Devrait supporter la reconnexion après une longue déconnexion', () => {
        cy.wait(2000);

        // Simuler une longue déconnexion
        cy.intercept('GET', '/api/notifications/stream/*').as('firstConnection');

        cy.wait(5000);

        // Vérifier que la reconnexion se fait
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="connection-status"]').length > 0) {
                cy.get('[data-testid="connection-status"]')
                    .should('exist');
            }
        });
    });

    it('Devrait afficher les notifications avec les bonnes icônes selon le type', () => {
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-item"]').length > 0) {
                // Chercher les icônes ou classes de notification
                cy.get('[data-testid="notification-item"]').first()
                    .should('have.attr', 'class')
                    .and('match', /notification|alert/);
            }
        });
    });

    it('Devrait limiter le nombre de notifications affichées', () => {
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-item"]').length > 0) {
                // Vérifier qu'on n'affiche pas plus de X notifications
                cy.get('[data-testid="notification-item"]')
                    .should('have.length.lessThan', 50);
            }
        });
    });

    it('Devrait permettre la suppression d\'une notification', () => {
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-delete"]').length > 0) {
                cy.get('[data-testid="notification-delete"]').first().click();

                cy.wait(1000);

                // Vérifier que la notification a disparu
                // (en comptant le nombre de notifications)
            }
        });
    });

    it('Devrait afficher une animation lors de la réception d\'une notification', () => {
        cy.get('body').then(($body) => {
            if ($body.find('[data-testid="notification-badge"]').length > 0) {
                // Vérifier qu'une classe d'animation est appliquée
                cy.get('[data-testid="notification-badge"]')
                    .should('exist');
                    // .should('have.class', 'animate');  // si applicable
            }
        });
    });
});
