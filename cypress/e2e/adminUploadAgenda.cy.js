describe('Administration - téléversement des agendas bénévoles', () => {
    const adminSession = {
        token: [
            'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9',
            'eyJzdWIiOiIxIiwiZW1haWwiOiJhbm5hQGV4YW1wbGUuY29tIiwicm9sZXMiOlsiQURNSU4iXSwiZXhwIjo0MTAyNDQ0ODAwfQ',
            'signature'
        ].join('.'),
        user: {
            id: '1',
            email: 'anna@example.com',
            roles: ['ADMIN']
        }
    };

    const visitAdminPage = () => {
        cy.visit('/admin', {
            onBeforeLoad(win) {
                win.localStorage.setItem('token', adminSession.token);
                win.localStorage.setItem('user', JSON.stringify(adminSession.user));
                win.localStorage.setItem('isAccountValidated', 'true');
                win.localStorage.removeItem('mustChangePassword');

                win.EventSource = class {
                    addEventListener() {}
                    close() {}
                };
            }
        });
    };

    const openUploadModal = () => {
        cy.contains('button', 'Téléverser les agendas').click();
        cy.get('.modal').should('be.visible');
        cy.contains('.modal-title', 'Téléversement des agendas bénévoles').should('be.visible');
    };

    beforeEach(() => {
        cy.on('uncaught:exception', () => false);
        visitAdminPage();
    });

    it('prévisualise un fichier JSON valide avant téléversement', () => {
        openUploadModal();

        cy.get('.modal input[type="file"]').selectFile('cypress/fixtures/agendas/uploadAgendaValid.json', {
            force: true
        });

        cy.contains('h6', 'Aperçu :').should('be.visible');
        cy.contains('.badge', '2 bénévole(s)').should('be.visible');
        cy.contains('.badge', '3 tâche(s)').should('be.visible');
        cy.get('.modal tbody tr').should('have.length', 2);
        cy.contains('lea.benevole@example.com').should('exist');
        cy.contains('Distribution de dossards').should('exist');
        cy.contains('Contrôle accès zone départ').should('exist');
        cy.contains('button', 'Téléverser (2 bénévole(s))').should('be.enabled');
    });

    it('affiche les erreurs de validation pour un fichier JSON invalide', () => {
        openUploadModal();

        cy.get('.modal input[type="file"]').selectFile('cypress/fixtures/agendas/uploadAgendaInvalid.json', {
            force: true
        });

        cy.contains('Erreurs de validation').should('be.visible');
        cy.contains('Entrée 1 : L\'email "email-invalide" n\'est pas valide').should('be.visible');
        cy.contains('Entrée 1 (email-invalide) : La liste des tâches ne peut pas être vide').should('be.visible');
        cy.contains('Entrée 2, tâche 1 : Le nom de la tâche est requis').should('be.visible');
        cy.contains('Entrée 2, tâche 1 : Le nom de la compétition est requis').should('be.visible');
        cy.contains('Entrée 2, tâche 1 : Le nom de l\'événement est requis').should('be.visible');
        cy.contains('button', 'Téléverser (0 bénévole(s))').should('be.disabled');
    });

    it('envoie le contenu du fichier et affiche le succès du téléversement', () => {
        cy.fixture('agendas/uploadAgendaValid.json').then((expectedAgendas) => {
            cy.intercept('POST', '**/admin/agenda/upload', {
                statusCode: 201,
                fixture: 'agendas/uploadAgendaResponse.json'
            }).as('uploadAgendas');

            openUploadModal();

            cy.get('.modal input[type="file"]').selectFile('cypress/fixtures/agendas/uploadAgendaValid.json', {
                force: true
            });

            cy.contains('button', 'Téléverser (2 bénévole(s))').click();

            cy.wait('@uploadAgendas').then(({ request }) => {
                expect(request.body).to.deep.equal(expectedAgendas);
            });

            cy.get('.modal').should('not.exist');
            cy.contains('✓ Téléversement terminé : 2/2 agenda(s) traité(s) avec succès.').should('be.visible');
        });
    });
});
