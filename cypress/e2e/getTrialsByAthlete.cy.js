describe("Tests - Visualisation des épreuves d'un sportif connecté", () => {

    const currentAthleteId = 3;

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

    it('display every trials of the current athlete', () => {
        const fixturePath = "get-trials/assignedTrialsWithPastAndFutur.json"
        const futurFixturePath = "get-trials/trialInFutur.json"
        const pastFixturePath = "get-trials/trialInPast.json"

        cy.intercept('GET', `**/public/trials/assigned/${currentAthleteId}`,
            { fixture: fixturePath}
        ).as('getTrials');

        cy.intercept('GET', '**/public/trials/1',
            { fixture: futurFixturePath}
        ).as(`getTrialDetail-1`);

        cy.intercept('GET', '**/public/trials/4',
            { fixture: pastFixturePath}
        ).as(`getTrialDetail-4`);

        cy.contains('Mes épreuves').click();

        cy.wait('@getTrials');
        cy.wait('@getTrialDetail-1');
        cy.wait('@getTrialDetail-4');

        cy.fixture(futurFixturePath).then((trial) => {
            cy.contains(trial.name).should('be.visible')
                .parentsUntil(".card")
                .get(".card-title")
                .contains("A venir").should('be.visible');
        })

        cy.fixture(pastFixturePath).then((trial) => {
            cy.contains(trial.name).should('be.visible')
                .parentsUntil(".card")
                .get(".card-title")
                .contains("Passés").should('be.visible');
        })

    });

    it('display athlete\'s ranking', () => {
        const fixturePath = "get-trials/assignedTrialsWithPastAndFutur.json"
        const pastFixturePath = "get-trials/trialInPastWithSoloRanking.json"

        cy.intercept('GET', `**/public/trials/assigned/${currentAthleteId}`,
            { fixture: fixturePath}
        ).as('getTrials');

        cy.intercept('GET', '**/public/trials/4',
            { fixture: pastFixturePath}
        ).as(`getTrialDetail-4`);

        cy.contains('Mes épreuves').click();

        cy.wait('@getTrials');
        cy.wait('@getTrialDetail-4');

        cy.fixture(pastFixturePath).then((trial) => {
            cy.contains(trial.name).should('be.visible')
                .parentsUntil(".card")
                .get(".card-title")
                .contains("Passés").should('be.visible')
                .parentsUntil(".card")
                .get(".card-body")
                .contains("2h05m").should('be.visible');
        })

    });

    it('display athlete\'s team ranking', () => {
        const fixturePath = "get-trials/assignedTrialsWithPastAndFutur.json"
        const pastFixturePath = "get-trials/trialInPastWithTeamRanking.json"

        cy.intercept('GET', `**/public/trials/assigned/${currentAthleteId}`,
            { fixture: fixturePath}
        ).as('getTrials');

        cy.intercept('GET', '**/public/trials/4',
            { fixture: pastFixturePath}
        ).as(`getTrialDetail-4`);

        cy.contains('Mes épreuves').click();

        cy.wait('@getTrials');
        cy.wait('@getTrialDetail-4');

        cy.fixture(pastFixturePath).then((trial) => {
            cy.contains(trial.name).should('be.visible')
                .parentsUntil(".card")
                .get(".card-title")
                .contains("Passés").should('be.visible')
                .parentsUntil(".card")
                .get(".card-body")
                .contains("2h05m").should('be.visible');
        })

    });

    it('display athlete\'s forfeit', () => {
        const fixturePath = "get-trials/assignedTrialsWithPastAndFutur.json"
        const pastFixturePath = "get-trials/trialInPastWithSoloRankingAndForfeit.json"

        cy.intercept('GET', `**/public/trials/assigned/${currentAthleteId}`,
            { fixture: fixturePath}
        ).as('getTrials');

        cy.intercept('GET', '**/public/trials/4',
            { fixture: pastFixturePath}
        ).as(`getTrialDetail-4`);

        cy.contains('Mes épreuves').click();

        cy.wait('@getTrials');
        cy.wait('@getTrialDetail-4');

        cy.fixture(pastFixturePath).then((trial) => {
            cy.contains(trial.name).should('be.visible')
                .parentsUntil(".card")
                .get(".card-title")
                .contains("Passés").should('be.visible')
                .parentsUntil(".card")
                .get(".card-body")
                .contains("Forfait").should('be.visible');
        })

    });

    it('display athlete\'s team forfeit', () => {
        const fixturePath = "get-trials/assignedTrialsWithPastAndFutur.json"
        const pastFixturePath = "get-trials/trialInPastWithTeamRankingAndForfeit.json"

        cy.intercept('GET', `**/public/trials/assigned/${currentAthleteId}`,
            { fixture: fixturePath}
        ).as('getTrials');

        cy.intercept('GET', '**/public/trials/4',
            { fixture: pastFixturePath}
        ).as(`getTrialDetail-4`);

        cy.contains('Mes épreuves').click();

        cy.wait('@getTrials');
        cy.wait('@getTrialDetail-4');

        cy.fixture(pastFixturePath).then((trial) => {
            cy.contains(trial.name).should('be.visible')
                .parentsUntil(".card")
                .get(".card-title")
                .contains("Passés").should('be.visible')
                .parentsUntil(".card")
                .get(".card-body")
                .contains("Forfait").should('be.visible');
        })

    });
});
