describe('AdminEpreuves – Administration des épreuves', () => {

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

    const defaultFixture = () => {
        cy.intercept('GET', '**/commissaire/trials', {
            fixture: 'adminEpreuves/trialsList.json'
        }).as('getTrials');
        interceptTrialResults();
    }

    const visitePage = () => {
        cy.contains('Gestion épreuves').click();
    }

    const waitDefaultFixtures = () => {
        cy.wait('@getTrials');
        cy.wait('@getResults1');
        cy.wait('@getResults2');
        cy.wait('@getResults3');
        cy.get('.spinner-border').should('not.exist');
    }

    beforeEach(() => {
        cy.on('uncaught:exception', () => false);

        cy.visit('/');
        cy.wait(1000);

        cy.contains('Connexion').click();
        cy.wait(1000);

        // Remplir le formulaire de connexion
        cy.get('input[type="email"]').type('commissaire@example.com');
        cy.wait(500);
        cy.get('input[type="password"]').type('test123');
        cy.wait(500);

        cy.get('button[type="submit"]').click();
        cy.wait(500);
    });

    // =========================================================================
    // Affichage général
    // =========================================================================

    it('affiche le titre principal de la page', () => {
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

        cy.contains('h1', 'Administration des épreuves').should('be.visible');
    });

    it('affiche l\'en-tête "Épreuves à gérer"', () => {
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

        cy.contains('.card-header', 'Épreuves à gérer').should('be.visible');
    });

    it('affiche les trois épreuves issues de la fixture', () => {
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

        cy.get('.card-title').should('have.length', 3);
        cy.contains('.card-title', '100m Solo Trial').should('be.visible');
        cy.contains('.card-title', '4x100m Relais Équipe').should('be.visible');
        cy.contains('.card-title', '200m Solo Trial').should('be.visible');
    });

    // =========================================================================
    // Badges type d'épreuve (Solo / Équipe)
    // =========================================================================

    it('affiche le badge "🏃 Solo" (bg-success) pour une épreuve individuelle', () => {
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

        cy.contains('.card-title', '100m Solo Trial')
            .closest('.card')
            .contains('.badge', '🏃 Solo')
            .should('be.visible')
            .and('have.class', 'bg-success');
    });

    it('affiche le badge "👥 Équipe" (bg-info) pour une épreuve par équipe', () => {
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

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
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

        cy.contains('.card-title', '100m Solo Trial')
            .closest('.card')
            .contains('.badge', '3 participant(s)')
            .should('be.visible');
    });

    it('affiche "2 participant(s)" pour l\'épreuve équipe à 2 inscrits', () => {
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

        cy.contains('.card-title', '4x100m Relais Équipe')
            .closest('.card')
            .contains('.badge', '2 participant(s)')
            .should('be.visible');
    });

    // =========================================================================
    // Badge résultats – couleurs et compteurs
    // =========================================================================

    it('affiche un badge warning (1/2) pour des résultats partiellement validés', () => {
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

        cy.contains('.card-title', '100m Solo Trial')
            .closest('.card')
            .contains('.badge', /résultat/)
            .should('have.class', 'bg-warning')
            .and('contain.text', '1/2');
    });

    it('affiche un badge success (2/2) quand tous les résultats sont validés', () => {
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

        cy.contains('.card-title', '4x100m Relais Équipe')
            .closest('.card')
            .contains('.badge', /résultat/)
            .should('have.class', 'bg-success')
            .and('contain.text', '2/2');
    });

    it('affiche un badge secondary (0/2) quand aucun résultat n\'est validé', () => {
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

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
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

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

    it('"Modifier les participants" navigue vers /commissaire/trials/1/participants', () => {
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

        // Intercepter les endpoints appelés par ManageParticipants au chargement
        cy.intercept('GET', '**/commissaire/trials/1/participants/full', {
            body: { registered: [], potential: [], trialName: '100m Solo Trial', teamTrial: false }
        }).as('getParticipantsFull');

        cy.contains('.card-title', '100m Solo Trial')
            .closest('.card')
            .contains('button', 'Modifier les participants')
            .click();
        cy.wait(500);
        cy.url().should('include', '/commissaire/trials/1/participants');
    });

    it('"Gérer les résultats" navigue vers /commissaire/trials/1/results', () => {
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

        // Intercepter l'endpoint rechargé par ManageResults
        cy.intercept('GET', '**/commissaire/trials/1/results', {
            fixture: 'adminEpreuves/results1_partiel.json'
        }).as('getResultsDetail');

        cy.contains('.card-title', '100m Solo Trial')
            .closest('.card')
            .contains('button', 'Gérer les résultats')
            .click();
        cy.wait(500);
        cy.url().should('include', '/commissaire/trials/1/results');
    });

    it('affiche le bouton "← Retour"', () => {
        defaultFixture()
        visitePage()
        waitDefaultFixtures()

        cy.contains('button', '← Retour').should('be.visible');
    });

    // =========================================================================
    // État vide
    // =========================================================================

    it('affiche "Aucune épreuve disponible" quand la liste retournée est vide', () => {
        cy.intercept('GET', '**/commissaire/trials', { body: [] }).as('getTrialsEmpty');
        visitePage()
        cy.wait('@getTrialsEmpty');
        cy.get('.spinner-border').should('not.exist');
        cy.wait(500);
        cy.contains('Aucune épreuve active à gérer.').should('be.visible');
    });

});
