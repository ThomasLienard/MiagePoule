describe('Page de détails trials', () => {
    beforeEach(() => {
        cy.visit('/public/trials/1');
    });

    it('check details trial team without results', () => {
        const fixturePath = "trialTeamWithoutResults.json"
        cy.intercept('GET', '**/public/trials/1',
            { fixture: fixturePath}
        ).as('getTrial');
        cy.wait('@getTrial');

        cy.fixture(fixturePath).then((jsonData) => {
            cy.contains(jsonData.name).should('be.visible');
            cy.contains(jsonData.place.name).should('be.visible');
            cy.contains(jsonData.place.description).should('be.visible');

            cy.contains(jsonData.description).should('be.visible');
            cy.contains(jsonData.competitionName).should('be.visible');

            cy.contains(jsonData.teamParticipants[0].name)
                .should('be.visible')
                .click()
                .wait(500);
            for (const participant of jsonData.teamParticipants[0].members) {
                cy.contains(participant.fullName).should('be.visible');
            }
        })

    });

    it('check details trial solo without results', () => {
        const fixturePath = "trialSoloWithoutResults.json"
        cy.intercept('GET', '**/public/trials/1',
            { fixture: fixturePath}
        ).as('getTrial');
        cy.wait('@getTrial');

        cy.fixture(fixturePath).then((jsonData) => {
            cy.contains(jsonData.name).should('be.visible');
            cy.contains(jsonData.place.name).should('be.visible');
            cy.contains(jsonData.place.description).should('be.visible');

            cy.contains(jsonData.description).should('be.visible');
            cy.contains(jsonData.competitionName).should('be.visible');

            for (const participant of jsonData.soloParticipants) {
                cy.contains(participant.fullName).should('be.visible');
            }
        })
    });

    it('check details trial team with results', () => {
        const fixturePath = "trialTeamWithResults.json"
        cy.intercept('GET', '**/public/trials/1',
            { fixture: fixturePath}
        ).as('getTrial');
        cy.wait('@getTrial');

        cy.fixture(fixturePath).then((jsonData) => {
            cy.contains(jsonData.name).should('be.visible');
            cy.contains(jsonData.place.name).should('be.visible');
            cy.contains(jsonData.place.description).should('be.visible');

            cy.contains(jsonData.description).should('be.visible');
            cy.contains(jsonData.competitionName).should('be.visible');

            cy.contains(jsonData.teamParticipants[0].name)
                .should('be.visible')
                .click()
                .wait(500);
            for (const participant of jsonData.teamParticipants[0].members) {
                cy.contains(participant.fullName).should('be.visible');
            }

            for(const ranking of jsonData.rankings) {
                cy.contains(ranking.result).should('be.visible');
            }
        })
    });

    it('check details trial solo with results', () => {
        const fixturePath = "trialSoloWithResults.json"
        cy.intercept('GET', '**/public/trials/1',
            { fixture: fixturePath}
        ).as('getTrial');
        cy.wait('@getTrial');

        cy.fixture(fixturePath).then((jsonData) => {
            cy.contains(jsonData.name).should('be.visible');
            cy.contains(jsonData.place.name).should('be.visible');
            cy.contains(jsonData.place.description).should('be.visible');

            cy.contains(jsonData.description).should('be.visible');
            cy.contains(jsonData.competitionName).should('be.visible');

            for (const participant of jsonData.soloParticipants) {
                cy.contains(participant.fullName).should('be.visible');
            }

            for(const ranking of jsonData.rankings) {
                cy.contains(ranking.result).should('be.visible');
            }
        })
    });
});
