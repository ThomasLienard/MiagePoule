describe('ManageResults – Gestion des résultats d\'une épreuve', () => {

    /** Charge la page des résultats pour le trial `id` avec la fixture donnée. */
    const visitResults = (fixturePath) => {
        cy.intercept('GET', `**/commissaire/trials/1/results`, {
            fixture: fixturePath
        }).as('getResults');
        cy.contains("Gérer les résultats").click();
        cy.wait(`@getResults`);
        cy.get('.spinner-border').should('not.exist');
    };

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
        cy.wait(1000);

        cy.intercept('GET', `**/commissaire/trials`, {
            fixture: "manageResults/trial_init.json"
        }).as("getInitTrial");

        cy.intercept('GET', `**/commissaire/trials/1/results`, {
            fixture: "manageResults/results_init.json"
        }).as("getInitResults");

        cy.contains("Gestion épreuves").click();

        cy.wait("@getInitTrial");
        cy.wait("@getInitResults");
        cy.wait(100);
    });

    // =========================================================================
    // Affichage initial – épreuve solo passée (partielle)
    // =========================================================================

    it('affiche le titre avec le nom de l\'épreuve', () => {
        visitResults('manageResults/results_partiel.json');
        cy.contains('h1', '100m Solo Trial').should('be.visible');
    });

    it('affiche le badge "🏃 Épreuve Solo" (bg-success) pour une épreuve solo', () => {
        visitResults('manageResults/results_partiel.json');
        cy.contains('.badge', '🏃 Épreuve Solo')
            .should('be.visible')
            .and('have.class', 'bg-success');
    });

    it('affiche le badge compteur "1/2 validé(s)" (bg-secondary) partiellement validé', () => {
        visitResults('manageResults/results_partiel.json');
        cy.contains('.badge', /validé/)
            .should('contain.text', '1/2')
            .and('have.class', 'bg-secondary');
    });

    it('affiche l\'en-tête "Résultats par athlète" pour une épreuve solo', () => {
        visitResults('manageResults/results_partiel.json');
        cy.contains('.card-header', 'Résultats par athlète').should('be.visible');
    });

    it('affiche 3 lignes de participants (2 non-forfaits + 1 forfait)', () => {
        visitResults('manageResults/results_partiel.json');
        cy.get('.card .card-body .card').should('have.length', 3);
    });

    // =========================================================================
    // StatusBadge
    // =========================================================================

    it('affiche le badge "✔ Validé" (bg-success) pour un participant validé', () => {
        visitResults('manageResults/results_partiel.json');
        cy.contains('.fw-semibold', 'Marie Athlete')
            .closest('.card')
            .contains('.badge', '✔ Validé')
            .should('have.class', 'bg-success');
    });

    it('affiche le badge "Non validé" (bg-secondary) pour un participant non validé', () => {
        visitResults('manageResults/results_partiel.json');
        cy.contains('.fw-semibold', 'John Smith')
            .closest('.card')
            .contains('.badge', 'Non validé')
            .should('have.class', 'bg-secondary');
    });

    it('affiche le badge "Forfait" (bg-danger) pour un participant forfait', () => {
        visitResults('manageResults/results_partiel.json');
        cy.contains('.fw-semibold', 'Forfait Guy')
            .closest('.card')
            .contains('.badge', 'Forfait')
            .should('have.class', 'bg-danger');
    });

    // =========================================================================
    // canEdit = true (épreuve passée) – champs et boutons actifs
    // =========================================================================

    it('le champ résultat de John Smith est activé (épreuve passée)', () => {
        visitResults('manageResults/results_partiel.json');
        cy.contains('.fw-semibold', 'John Smith')
            .closest('.card')
            .find('input[type="text"]')
            .should('not.be.disabled');
    });

    it('le bouton "✔ Valider" de John Smith est actif car il a un résultat', () => {
        visitResults('manageResults/results_partiel.json');
        cy.contains('.fw-semibold', 'John Smith')
            .closest('.card')
            .contains('button', '✔ Valider')
            .should('not.be.disabled');
    });

    it('le bouton "✕ Invalider" de Marie est affiché car elle est déjà validée', () => {
        visitResults('manageResults/results_partiel.json');
        cy.contains('.fw-semibold', 'Marie Athlete')
            .closest('.card')
            .contains('button', '✕ Invalider')
            .should('be.visible');
    });

    it('le champ du forfait est désactivé', () => {
        visitResults('manageResults/results_partiel.json');
        cy.contains('.fw-semibold', 'Forfait Guy')
            .closest('.card')
            .find('input[type="text"]')
            .should('be.disabled');
    });

    it('aucun bouton Valider/Invalider n\'est affiché pour le forfait', () => {
        visitResults('manageResults/results_partiel.json');
        cy.contains('.fw-semibold', 'Forfait Guy')
            .closest('.card')
            .contains('button', /Valider|Invalider/)
            .should('not.exist');
    });

    // =========================================================================
    // canEdit = false (épreuve future) – verrouillage
    // =========================================================================

    it('affiche la bannière de verrouillage pour une épreuve future', () => {
        visitResults('manageResults/results_future.json');
        cy.contains('La saisie des résultats est').should('be.visible');
        cy.contains('verrouillée').should('be.visible');
    });

    it('les champs de saisie sont désactivés pour une épreuve future', () => {
        visitResults('manageResults/results_future.json');
        cy.get('input[type="text"]').each(($input) => {
            cy.wrap($input).should('be.disabled');
        });
    });

    it('le bouton "Tout enregistrer" est désactivé pour une épreuve future', () => {
        visitResults('manageResults/results_future.json');
        cy.contains('button', /Tout enregistrer/).should('be.disabled');
    });

    it('le bouton "Valider tout" est désactivé pour une épreuve future', () => {
        visitResults('manageResults/results_future.json');
        cy.contains('button', /Valider tout/).should('be.disabled');
    });

    // =========================================================================
    // Épreuve équipe
    // =========================================================================

    it('affiche le badge "👥 Épreuve Équipe" (bg-info) pour une épreuve équipe', () => {
        visitResults('manageResults/results_equipe_valides.json');
        cy.contains('.badge', '👥 Épreuve Équipe')
            .should('be.visible')
            .and('have.class', 'bg-info');
    });

    it('affiche l\'en-tête "Résultats par équipe" pour une épreuve équipe', () => {
        visitResults('manageResults/results_equipe_valides.json');
        cy.contains('.card-header', 'Résultats par équipe').should('be.visible');
    });

    it('affiche le badge compteur "2/2 validé(s)" (bg-success) quand tous validés', () => {
        visitResults('manageResults/results_equipe_valides.json');
        cy.contains('.badge', /validé/)
            .should('contain.text', '2/2')
            .and('have.class', 'bg-success');
    });

    // =========================================================================
    // Aucun participant
    // =========================================================================

    it('affiche "Aucun participant inscrit" quand la liste est vide', () => {
        visitResults('manageResults/results_vide.json');
        cy.contains('Aucun participant inscrit').should('be.visible');
    });

    it('les boutons "Tout enregistrer" et "Valider tout" sont désactivés si aucun participant', () => {
        visitResults('manageResults/results_vide.json');
        cy.contains('button', /Tout enregistrer/).should('be.disabled');
        cy.contains('button', /Valider tout/).should('be.disabled');
    });

    // =========================================================================
    // Saisie et enregistrement d'un résultat individuel
    // =========================================================================

    it('saisir et enregistrer un résultat → appel PUT et message de succès', () => {
        visitResults('manageResults/results_partiel.json');

        cy.intercept('PUT', '**/commissaire/trials/1/results', {
            body: {
                participantId: 2,
                participantName: 'John Smith',
                participantType: 'ATHLETE',
                result: '11.8s',
                isValidated: false,
                isForfeit: false
            }
        }).as('setResult');

        cy.contains('.fw-semibold', 'John Smith')
            .closest('.card')
            .within(() => {
                cy.get('input[type="text"]').clear().type('11.8s');
                cy.wait(500);
                cy.contains('button', 'enregistrer').click();
                cy.wait(500);
            });

        cy.wait('@setResult');
        cy.contains('.alert-success', /John Smith/).should('be.visible');
    });

    // =========================================================================
    // Tout enregistrer (bulk)
    // =========================================================================

    it('"Tout enregistrer" → appel PUT bulk et message de succès', () => {
        visitResults('manageResults/results_partiel.json');

        cy.intercept('PUT', '**/commissaire/trials/1/results/bulk', {
            body: [
                { participantId: 1, participantName: 'Marie Athlete', participantType: 'ATHLETE', result: '11.5s', isValidated: true, isForfeit: false },
                { participantId: 2, participantName: 'John Smith', participantType: 'ATHLETE', result: '12.0s', isValidated: false, isForfeit: false }
            ]
        }).as('setBulk');

        cy.contains('button', /Tout enregistrer/).click();
        cy.wait(500);
        cy.wait('@setBulk');
        cy.contains('.alert-success', /enregistrés/).should('be.visible');
    });

    // =========================================================================
    // Validation individuelle
    // =========================================================================

    it('cliquer "✔ Valider" → appel POST validate et badge passe à "✔ Validé"', () => {
        visitResults('manageResults/results_partiel.json');

        cy.intercept('POST', '**/commissaire/trials/1/results/validate', {
            body: {
                participantId: 2,
                participantName: 'John Smith',
                participantType: 'ATHLETE',
                result: '12.0s',
                isValidated: true,
                isForfeit: false
            }
        }).as('validateResult');

        cy.contains('.fw-semibold', 'John Smith')
            .closest('.card')
            .contains('button', '✔ Valider')
            .click();
        cy.wait(500);
        cy.wait('@validateResult');
        cy.contains('.fw-semibold', 'John Smith')
            .closest('.card')
            .contains('.badge', '✔ Validé')
            .should('have.class', 'bg-success');
    });

    // =========================================================================
    // Invalidation individuelle
    // =========================================================================

    it('cliquer "✕ Invalider" → appel POST invalidate et message de succès', () => {
        visitResults('manageResults/results_partiel.json');

        cy.intercept('POST', '**/commissaire/trials/1/results/invalidate', {
            body: {
                participantId: 1,
                participantName: 'Marie Athlete',
                participantType: 'ATHLETE',
                result: '11.5s',
                isValidated: false,
                isForfeit: false
            }
        }).as('invalidateResult');

        cy.contains('.fw-semibold', 'Marie Athlete')
            .closest('.card')
            .contains('button', '✕ Invalider')
            .click();
        cy.wait(500);
        cy.wait('@invalidateResult');
        cy.contains('.alert-success', /Marie Athlete/).should('be.visible');
    });

    it('après invalidation, le badge de Marie repasse à "Non validé"', () => {
        visitResults('manageResults/results_partiel.json');

        cy.intercept('POST', '**/commissaire/trials/1/results/invalidate', {
            body: {
                participantId: 1,
                participantName: 'Marie Athlete',
                participantType: 'ATHLETE',
                result: '11.5s',
                isValidated: false,
                isForfeit: false
            }
        }).as('invalidateResult');

        cy.contains('.fw-semibold', 'Marie Athlete')
            .closest('.card')
            .contains('button', '✕ Invalider')
            .click();
        cy.wait(500);
        cy.wait('@invalidateResult');
        cy.contains('.fw-semibold', 'Marie Athlete')
            .closest('.card')
            .contains('.badge', 'Non validé')
            .should('have.class', 'bg-secondary');
    });

    // =========================================================================
    // Valider tout (modal de confirmation)
    // =========================================================================

    it('cliquer "Valider tout" ouvre la modal de confirmation', () => {
        visitResults('manageResults/results_complet.json');
        cy.contains('button', /Valider tout/).click();
        cy.wait(500);
        cy.get('.modal').should('be.visible');
        cy.contains('.modal', 'Valider tous les résultats').should('be.visible');
        cy.contains('.modal', '100m Solo Trial').should('be.visible');
    });

    it('"Annuler" dans la modal ferme la modal sans appel API', () => {
        visitResults('manageResults/results_complet.json');
        cy.contains('button', /Valider tout/).click();
        cy.wait(500);
        cy.get('.modal').should('be.visible');
        cy.get('.modal').contains('button', 'Annuler').click();
        cy.wait(500);
        cy.get('.modal').should('not.exist');
    });

    it('confirmer dans la modal → appel POST validate-all et message de succès', () => {
        visitResults('manageResults/results_complet.json');

        const validatedAll = {
            trialId: 1,
            trialName: '100m Solo Trial',
            teamTrial: false,
            startTime: '2025-01-01T09:00:00',
            results: [
                { participantId: 1, participantName: 'Marie Athlete', participantType: 'ATHLETE', result: '11.5s', isValidated: true, isForfeit: false },
                { participantId: 2, participantName: 'John Smith', participantType: 'ATHLETE', result: '12.0s', isValidated: true, isForfeit: false },
                { participantId: 3, participantName: 'Forfait Guy', participantType: 'ATHLETE', result: null, isValidated: false, isForfeit: true }
            ]
        };

        cy.intercept('POST', '**/commissaire/trials/1/results/validate-all', {
            body: validatedAll
        }).as('validateAll');

        cy.contains('button', /Valider tout/).click();
        cy.wait(500);
        cy.get('.modal').contains('button', /Valider tout/).click();
        cy.wait(500);
        cy.wait('@validateAll');
        cy.contains('.alert-success', /validés/).should('be.visible');
    });

    it('après validation globale, le badge compteur passe à "2/2 validé(s)" (bg-success)', () => {
        visitResults('manageResults/results_complet.json');

        cy.intercept('POST', '**/commissaire/trials/1/results/validate-all', {
            body: {
                trialId: 1,
                trialName: '100m Solo Trial',
                teamTrial: false,
                startTime: '2025-01-01T09:00:00',
                results: [
                    { participantId: 1, participantName: 'Marie Athlete', participantType: 'ATHLETE', result: '11.5s', isValidated: true, isForfeit: false },
                    { participantId: 2, participantName: 'John Smith', participantType: 'ATHLETE', result: '12.0s', isValidated: true, isForfeit: false },
                    { participantId: 3, participantName: 'Forfait Guy', participantType: 'ATHLETE', result: null, isValidated: false, isForfeit: true }
                ]
            }
        }).as('validateAll');

        cy.contains('button', /Valider tout/).click();
        cy.wait(500);
        cy.get('.modal').contains('button', /Valider tout/).click();
        cy.wait(500);
        cy.wait('@validateAll');
        cy.contains('.badge', /validé/)
            .should('contain.text', '2/2')
            .and('have.class', 'bg-success');
    });

    // =========================================================================
    // Navigation – retour
    // =========================================================================

    it('cliquer "← Retour aux épreuves" navigue vers /commissaire/trials', () => {
        cy.intercept(/localhost:8084/, { statusCode: 200, body: {} });
        cy.intercept('GET', '**/commissaire/trials', { body: [] });
        visitResults('manageResults/results_partiel.json');
        cy.contains('button', '← Retour aux épreuves').click();
        cy.wait(500);
        cy.url().should('include', '/commissaire/trials');
    });
});
