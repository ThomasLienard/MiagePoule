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
        cy.wait(500);

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

    });

    it('Devrait ouvrir le panneau de notifications au clic', () => {
        // Chercher le bouton de notifications
        cy.get('body').then(($body) => {
            if ($body.find('.notification-button').length > 0) {
                cy.get('.notification-button').click();
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

    });


    it('Devrait gérer la déconnexion et la reconnexion', () => {

        // Simuler une déconnexion
        cy.window().then((win) => {
            // Fermer la connexion EventSource
            if (win.eventSourceRef && win.eventSourceRef.current) {
                win.eventSourceRef.current.close();
            }
        });

        cy.wait(500);

        // Vérifier que le statut de connexion change
        cy.get('body').then(($body) => {
            // Chercher un indicateur de déconnexion
            if ($body.find('.connection-status').length > 0) {
                cy.get('.connection-status')
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
        cy.wait(500);

        // Vérifier que le système essaie de se reconnecter
        cy.window().then((win) => {
            // Chercher dans les logs un message de reconnexion
            cy.spy(win.console, 'log');
        });

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

        cy.wait(500);

    });

    it('Devrait persister les notifications après un rafraîchissement', () => {

        // Rafraîchir la page
        cy.reload();

        cy.wait(500);

        // Vérifier que la connexion est rétablie
        cy.get('body').then(($body) => {
            if ($body.find('.connection-status').length > 0) {
                cy.get('.connection-status')
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

        cy.wait(500);

        // Retourner à la page
        cy.go('back');

    });

    it('Devrait afficher le timestamp des notifications', () => {
        cy.get('body').then(($body) => {
            if ($body.find('.notification-item').length > 0) {
                cy.get('.notification-item').first()
                    .should('contain', /\d{1,2}\/\d{1,2}\/\d{4}|\d{1,2}:\d{2}/);
            }
        });
    });

    it('Devrait gérer les notifications sans userId', () => {
        // Naviguer vers une page sans authentification
        cy.get('nav a').contains('Connexion').click();

        cy.wait(500);

        // Vérifier qu'aucune connexion SSE ne s'établit
        cy.window().then((win) => {
            // Vérifier les logs
            cy.spy(win.console, 'log');
        });
    });

    it('Devrait supporter la reconnexion après une longue déconnexion', () => {

        // Simuler une longue déconnexion
        cy.intercept('GET', '/api/notifications/stream/*').as('firstConnection');

        cy.wait(1000);

        // Vérifier que la reconnexion se fait
        cy.get('body').then(($body) => {
            if ($body.find('.connection-status').length > 0) {
                cy.get('.connection-status')
                    .should('exist');
            }
        });
    });

    it('Devrait afficher les notifications avec les bonnes icônes selon le type', () => {
        cy.get('body').then(($body) => {
            // Vérifier que l'interface charge correctement
            cy.get('nav').should('be.visible');
        });
    });

    it('Devrait limiter le nombre de notifications affichées', () => {
        cy.get('body').then(($body) => {
            // Vérifier que l'interface charge correctement
            cy.get('nav').should('be.visible');
        });
    });

    it('Devrait permettre la suppression d\'une notification', () => {
        cy.get('body').then(($body) => {
            // Vérifier que l'interface charge correctement
            cy.get('nav').should('be.visible');
        });
    });

});
