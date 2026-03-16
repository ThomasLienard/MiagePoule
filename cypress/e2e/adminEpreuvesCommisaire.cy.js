describe('AdminEpreuves – Administration des épreuves', () => {

    // Entête JWT HS256 standard (base64url)
    const JWT_HEADER = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9';

    /**
     * Injecte un token commissaire valide dans le localStorage du navigateur
     * avant le chargement de la page (onBeforeLoad).
     */
    const setCommissaireAuth = (win) => {
        const payload = win.btoa(JSON.stringify({
            sub: '1',
            email: 'commissaire@example.com',
            roles: ['COMMISSAIRE'],
            iat: 1000000000,
            exp: 9999999999
        }));
        win.localStorage.setItem('token', `${JWT_HEADER}.${payload}.sig`);
        win.localStorage.setItem('user', JSON.stringify({
            id: '1',
            email: 'commissaire@example.com',
            roles: ['COMMISSAIRE']
        }));
    };

    /** Intercepte les appels résultats pour les 3 épreuves de la fixture de base. */
    const interceptTrialResults = () => {
        cy.intercept('GET', '**/commissaire/trials/1/results', {
            fixture: 'adminEpreuves/results1_partiel.json'
        }).as('getResults1');
        cy.intercept('GET', '**/commissaire/trials/2/results', {
            fixture: 'adminEpreuves/results2_tous.json'
        }).as('getResults2');
        cy.intercept('GET', '**/commissaire/trials/3/results', {
            fixture: 'adminEpreuves/results3_aucun.json'
        }).as('getResults3');
    };

    beforeEach(() => {
        cy.on('uncaught:exception', () => false);

        cy.intercept(/localhost:8084/, { statusCode: 200, body: {} });

        cy.intercept('GET', '**/commissaire/trials', {
            fixture: 'adminEpreuves/trialsList.json'
        }).as('getTrials');
        interceptTrialResults();

        cy.visit('/commissaire/trials', { onBeforeLoad: setCommissaireAuth });

        // Attendre la fin de tous les appels réseau avant les assertions
        cy.wait('@getTrials');
        cy.wait('@getResults1');
        cy.wait('@getResults2');
        cy.wait('@getResults3');
        cy.get('.spinner-border').should('not.exist');
    });

    // =========================================================================
    // Affichage général
    // =========================================================================

    it('affiche le titre principal de la page', () => {
        cy.contains('h1', 'Administration des épreuves').should('be.visible');
    });

    it('affiche l\'en-tête "Vos épreuves"', () => {
        cy.contains('.card-header', 'Vos épreuves').should('be.visible');
    });

    it('affiche les trois épreuves issues de la fixture', () => {
        cy.get('.card-title').should('have.length', 3);
        cy.contains('.card-title', '100m Solo Trial').should('be.visible');
        cy.contains('.card-title', '4x100m Relais Équipe').should('be.visible');
        cy.contains('.card-title', '200m Solo Trial').should('be.visible');
    });

    // =========================================================================
    // Badges type d'épreuve (Solo / Équipe)
    // =========================================================================

    it('affiche le badge "🏃 Solo" (bg-success) pour une épreuve individuelle', () => {
        cy.contains('.card-title', '100m Solo Trial')
            .closest('.card')
            .contains('.badge', '🏃 Solo')
            .should('be.visible')
            .and('have.class', 'bg-success');
    });

    it('affiche le badge "👥 Équipe" (bg-info) pour une épreuve par équipe', () => {
        cy.contains('.card-title', '4x100m Relais Équipe')
            .closest('.card')
            .contains('.badge', '👥 Équipe')
            .should('be.visible')
            .and('have.class', 'bg-info');
    });

    // =========================================================================
    // Badge nombre de participants
    // =========================================================================

    it('affiche "3 participant(s)" pour l\'épreuve solo à 3 inscrits', () => {
        cy.contains('.card-title', '100m Solo Trial')
            .closest('.card')
            .contains('.badge', '3 participant(s)')
            .should('be.visible');
    });

    it('affiche "2 participant(s)" pour l\'épreuve équipe à 2 inscrits', () => {
        cy.contains('.card-title', '4x100m Relais Équipe')
            .closest('.card')
            .contains('.badge', '2 participant(s)')
            .should('be.visible');
    });

    // =========================================================================
    // Badge résultats – couleurs et compteurs
    // =========================================================================

    it('affiche un badge warning (1/2) pour des résultats partiellement validés', () => {
        cy.contains('.card-title', '100m Solo Trial')
            .closest('.card')
            .contains('.badge', /résultat/)
            .should('have.class', 'bg-warning')
            .and('contain.text', '1/2');
    });

    it('affiche un badge success (2/2) quand tous les résultats sont validés', () => {
        cy.contains('.card-title', '4x100m Relais Équipe')
            .closest('.card')
            .contains('.badge', /résultat/)
            .should('have.class', 'bg-success')
            .and('contain.text', '2/2');
    });

    it('affiche un badge secondary (0/2) quand aucun résultat n\'est validé', () => {
        cy.contains('.card-title', '200m Solo Trial')
            .closest('.card')
            .contains('.badge', /résultat/)
            .should('have.class', 'bg-secondary')
            .and('contain.text', '0/2');
    });

    // =========================================================================
    // Exclusion des forfaits du décompte
    // =========================================================================

    it('exclut les forfaits : le total affiché est 2 et non 3 (trial 1 a 2 non-forfaits + 1 forfait)', () => {
        cy.contains('.card-title', '100m Solo Trial')
            .closest('.card')
            .contains('.badge', /résultat/)
            .invoke('text')
            .should('match', /\d\/2/)
            .and('not.match', /\d\/3/);
    });

    // =========================================================================
    // Navigation – boutons d'action
    // =========================================================================

    it('"Modifier participants" navigue vers /commissaire/trials/1/participants', () => {
        // Intercepter les endpoints appelés par ManageParticipants au chargement
        cy.intercept('GET', '**/commissaire/trials/1/participants/full', {
            body: { registered: [], potential: [], trialName: '100m Solo Trial', teamTrial: false }
        }).as('getParticipantsFull');

        cy.contains('.card-title', '100m Solo Trial')
            .closest('.card')
            .contains('button', 'Modifier participants')
            .click();
        cy.wait(500);
        cy.url().should('include', '/commissaire/trials/1/participants');
    });

    it('"Gérer résultats" navigue vers /commissaire/trials/1/results', () => {
        // Intercepter l'endpoint rechargé par ManageResults
        cy.intercept('GET', '**/commissaire/trials/1/results', {
            fixture: 'adminEpreuves/results1_partiel.json'
        }).as('getResultsDetail');

        cy.contains('.card-title', '100m Solo Trial')
            .closest('.card')
            .contains('button', 'Gérer résultats')
            .click();
        cy.wait(500);
        cy.url().should('include', '/commissaire/trials/1/results');
    });

    it('affiche le bouton "← Retour"', () => {
        cy.contains('button', '← Retour').should('be.visible');
    });

    // =========================================================================
    // État vide
    // =========================================================================

    it('affiche "Aucune épreuve disponible" quand la liste retournée est vide', () => {
        cy.intercept(/localhost:8084/, { statusCode: 200, body: {} });
        cy.intercept('GET', '**/commissaire/trials', { body: [] }).as('getTrialsEmpty');
        cy.visit('/commissaire/trials', { onBeforeLoad: setCommissaireAuth });
        cy.wait('@getTrialsEmpty');
        cy.get('.spinner-border').should('not.exist');
        cy.wait(500);
        cy.contains('Aucune épreuve disponible').should('be.visible');
    });

    // =========================================================================
    // État d'erreur
    // =========================================================================

    it('affiche une alerte .alert-danger en cas d\'erreur 500 au chargement', () => {
        cy.intercept(/localhost:8084/, { statusCode: 200, body: {} });
        cy.intercept('GET', '**/commissaire/trials', {
            statusCode: 500,
            body: { message: 'Erreur serveur interne' }
        }).as('getTrialsError');
        cy.visit('/commissaire/trials', { onBeforeLoad: setCommissaireAuth });
        cy.wait('@getTrialsError');
        cy.wait(500);
        cy.get('.alert-danger').should('be.visible');
    });
});
