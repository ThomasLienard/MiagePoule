describe("Tests - Déclaration de forfait par un sportif", () => {

    beforeEach(() => {
        // Désactive l'arrêt du test sur les exceptions non gérées
        cy.on('uncaught:exception', () => false);

        // Visite la page d'accueil
        cy.visit('/');
        cy.wait(2000);

        // Connexion en tant que athlete
        cy.contains('Connexion').click();
        cy.wait(2000);

        // Remplir le formulaire de connexion
        cy.get('input[type="email"]').type('athlete@example.com');
        cy.wait(500);
        cy.get('input[type="password"]').type('test123');
        cy.wait(500);

        // Soumettre le formulaire
        cy.get('button[type="submit"]').click();
        cy.wait(3000);

    });

    it('Scénario : Le modal de forfait peut être annulé', () => {
        // Intercepter l'appel pour récupérer les trials
        cy.intercept('GET', '**/public/trials/assigned/*', {
            fixture: 'athletes/declareWithdrawNotForfeit.json'
        }).as('getTrials');
     
        cy.intercept('GET', '**/public/events/9',
            { fixture: 'get-trials/trialInFutur.json'}
        ).as(`getTrialDetail-1`);

        cy.intercept('GET', '**/public/events/5',
            { fixture: 'get-trials/trialInPast.json'}
        ).as(`getTrialDetail-4`);

        cy.contains('Mes épreuves').click();

        cy.wait('@getTrials');
        cy.wait('@getTrialDetail-1');
        cy.wait('@getTrialDetail-4');

        // Cliquer sur le bouton "Déclarer forfait"
        cy.contains('button', 'Déclarer forfait').first().click();
        cy.wait(1000);
        
        // Vérifier que la modal s'affiche
        cy.get('.modal').should('be.visible');
        
        // Cliquer sur "Annuler"
        cy.get('.modal').within(() => {
            cy.contains('button', 'Annuler').click();
        });
        cy.wait(500);
        
        // Vérifier que la modal se ferme sans déclarer forfait
        cy.get('.modal').should('not.exist');
        cy.contains('Forfait déclaré avec succès').should('not.exist');
    });

    it('Scénario : Un sportif peut déclarer forfait à une épreuve future', () => {
        // Intercepter l'appel pour récupérer les trials
        cy.intercept('GET', '**/public/trials/assigned/*', {
            fixture: 'athletes/declareWithdrawNotForfeit.json'
        }).as('getTrials');

        // Intercepter l'appel POST pour déclarer forfait
        cy.intercept('POST', '**/athlete/trials/*/forfeit', {
            statusCode: 200,
            body: { message: 'Forfait déclaré avec succès' }
        }).as('declareForfeit');

        cy.intercept('GET', '**/public/events/9',
            { fixture: 'get-trials/trialInFutur.json'}
        ).as(`getTrialDetail-1`);

        cy.intercept('GET', '**/public/events/5',
            { fixture: 'get-trials/trialInPast.json'}
        ).as(`getTrialDetail-4`);

        cy.contains('Mes épreuves').click();

        cy.wait('@getTrials');
        cy.wait('@getTrialDetail-1');
        cy.wait('@getTrialDetail-4');

        // Vérifier que la page affiche les épreuves
        cy.contains('A venir').should('be.visible');
        
        // Cliquer sur le premier bouton "Déclarer forfait"
        cy.contains('button', 'Déclarer forfait').first().click();
        cy.wait(1000);
        
        // Vérifier que la modal de confirmation s'affiche
        cy.get('.modal').should('be.visible');
        cy.contains('Confirmer le forfait').should('be.visible');
        cy.wait(500);
        
        // Cliquer sur "Confirmer le forfait"
        cy.get('.modal').within(() => {
            cy.contains('button', 'Confirmer le forfait').click();
        });
        cy.wait('@declareForfeit');
        cy.wait(1000);
        
        // Vérifier que la modal se ferme et un message de succès apparaît
        cy.get('.modal').should('not.exist');
        cy.contains('Forfait déclaré avec succès').should('be.visible');
        cy.wait(1000);
        
        // Vérifier que le badge "Forfait déclaré" apparaît au lieu du bouton
        cy.contains('.badge', 'Forfait déclaré').should('be.visible');
    });

    it('Scénario : Un sportif ne peut pas déclarer forfait deux fois', () => {
        // Intercepter l'appel pour récupérer les trials avec un forfait déjà déclaré
        cy.intercept('GET', '**/public/trials/assigned/*', {
            fixture: 'athletes/declareWithdrawForfeit.json'
        }).as('getTrialsForfeit');

        cy.intercept('GET', '**/public/events/9',
            { fixture: 'get-trials/trialInFutur.json'}
        ).as(`getTrialDetail-1`);

        cy.intercept('GET', '**/public/events/5',
            { fixture: 'get-trials/trialInPast.json'}
        ).as(`getTrialDetail-4`);

        cy.contains('Mes épreuves').click();

        cy.wait('@getTrialsForfeit');
        cy.wait('@getTrialDetail-1');
        cy.wait('@getTrialDetail-4');

        // Vérifier que la page affiche les épreuves
        cy.contains('A venir').should('be.visible');
        
        // Vérifier que le badge "Forfait déclaré" est affiché pour la première épreuve
        cy.contains('.badge', 'Forfait déclaré').should('be.visible');
        
        // Vérifier que le bouton "Déclarer forfait" n'est pas présent pour l'épreuve où le forfait a été déclaré
        cy.contains('.badge', 'Forfait déclaré').parent().parent().within(() => {
            cy.contains('button', 'Déclarer forfait').should('not.exist');
        });
        
        // Recharger la page pour vérifier la persistance
        cy.reload();
        cy.wait('@getTrialsForfeit');
        cy.wait(2000);
        
        // Vérifier que le badge persiste après rechargement
        cy.contains('.badge', 'Forfait déclaré').should('be.visible');
    });

    it('Scénario : Les épreuves passées ne devraient pas avoir de bouton forfait', () => {
        // Intercepter l'appel pour récupérer les trials
        cy.intercept('GET', '**/public/trials/assigned/*', {
            fixture: 'athletes/declareWithdrawNotForfeit.json'
        }).as('getTrials');
        
        cy.intercept('GET', '**/public/events/9',
            { fixture: 'get-trials/trialInFutur.json'}
        ).as(`getTrialDetail-1`);

        cy.intercept('GET', '**/public/events/5',
            { fixture: 'get-trials/trialInPast.json'}
        ).as(`getTrialDetail-4`);

        cy.contains('Mes épreuves').click();

        cy.wait('@getTrials');
        cy.wait('@getTrialDetail-1');
        cy.wait('@getTrialDetail-4');

        // Vérifier la section "Passés"
        cy.contains('Passés').should('be.visible');
        
        // Vérifier qu'aucun bouton "Déclarer forfait" n'apparaît dans la section "Passés"
        cy.contains('Passés').parent().within(() => {
            cy.contains('button', 'Déclarer forfait').should('not.exist');
        });
    });
});
