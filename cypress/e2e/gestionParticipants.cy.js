describe('Tests - Gestion des Participants aux Épreuves', () => {

    const trialsFixturePath = "gestion-participants/trials.json"

    beforeEach(() => {
        // Désactive l'arrêt du test sur les exceptions non gérées
        cy.on('uncaught:exception', () => false);
        
        // Visite la page d'accueil
        cy.visit('/');
        cy.wait(2000);
        
        // Connexion en tant que commissaire
        cy.contains('Connexion').click();
        cy.wait(1000);
        
        // Remplir le formulaire de connexion
        cy.get('input[type="email"]').type('commissaire@example.com');
        cy.get('input[type="password"]').type('test123');

        // Soumettre le formulaire
        cy.get('button[type="submit"]').click();
        cy.wait(1000);

        cy.intercept('GET','**/commissaire/trials',
            {fixture: trialsFixturePath}
        ).as('getTrials')
        
        // Naviguer vers la page de gestion des participants
        cy.contains('Gestion épreuves').click();
        cy.wait('@getTrials');
        cy.wait(500);
    });

    it('Scénario : Affichage de la page de gestion des participants', () => {
        const participantsFixturePath = 'gestion-participants/allParticipants.json'
        cy.intercept('GET','**/commissaire/trials/1/participants/full',
            {fixture: participantsFixturePath}
        ).as('getParticipants')

        // Cliquer sur le bouton pour gérer une épreuve
        cy.get('button').contains('Modifier participants').first().click();
        cy.wait("@getParticipants");
        cy.wait(500);

        cy.contains('Ajouter un participant').should('be.visible');
        cy.contains('Équipes potentielles').should('be.visible');
        cy.contains('Participants inscrits').should('be.visible');
        cy.get('.badge').should('contain.text', 'Épreuve en équipe');

        cy.fixture(participantsFixturePath).then((trial) => {
            for (const participant of trial.participants) {
                cy.contains('Participants inscrits')
                    .parent()
                    .get(".card-body")
                    .contains(participant.name).should('be.visible')
                    .parent()
                    .contains(participant.country).should('be.visible')
            }
            for (const participant of trial.potentialAthletes) {
                cy.contains(participant.name).should('not.exist')
            }
            for (const participant of trial.potentialTeams) {
                cy.contains('Équipes potentielles')
                    .parent()
                    .get(".card-body")
                    .contains(participant.name).should('be.visible')
                    .parent()
                    .contains(participant.country).should('be.visible')
            }
        })
    });

    it('Scénario : Ajouter un participant via bouton "+ Ajouter"', () => {
        const participantsFixturePath = 'gestion-participants/allParticipants.json'
        cy.intercept('GET','**/commissaire/trials/1/participants/full',
            {fixture: participantsFixturePath}
        ).as('getParticipants')

        // Cliquer sur le bouton pour gérer une épreuve
        cy.get('button').contains('Modifier participants').first().click();
        cy.wait("@getParticipants");
        cy.wait(500);

        cy.contains('button', '+ Ajouter').first().click();
        cy.wait(500);

        // Vérifier que la modal de détails s'affiche
        cy.get('.modal').should('be.visible');
        cy.fixture(participantsFixturePath).then((trial) => {
            const firstTeamParticipant = trial.potentialTeams[0]
            cy.contains('Nom :').should('be.visible');
            cy.get('.modal').within(() => {
                cy.contains(firstTeamParticipant.name).should('be.visible');
                cy.contains(firstTeamParticipant.country).should('be.visible');
            });
        })

        const getAllNewParticipantsFixturePath = 'gestion-participants/addingParticipantToAllParticipants.json'

        cy.intercept('POST','**/commissaire/trials/1/participants',
            {statusCode : 200}
        ).as('addParticipant')

        cy.intercept('GET','**/commissaire/trials/1/participants/full',
            {fixture : getAllNewParticipantsFixturePath}
        ).as('getAllNewParticipants')

        // Cliquer sur "Inscrire le participant"
        cy.get('.modal').within(() => {
            cy.contains('button', 'Inscrire le participant').click();
        });
        cy.wait('@addParticipant');
        cy.wait('@getAllNewParticipants');
        cy.wait(500);

        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');

        cy.fixture(getAllNewParticipantsFixturePath).then((trial) => {
            for (const participant of trial.participants) {
                cy.contains('Participants inscrits')
                    .parent()
                    .get(".card-body")
                    .contains(participant.name).should('be.visible')
                    .parent()
                    .contains(participant.country).should('be.visible')
            }
        })
    });

    it('Scénario : Déclarer un participant en forfait', () => {
        const participantsFixturePath = 'gestion-participants/allParticipants.json'
        cy.intercept('GET','**/commissaire/trials/1/participants/full',
            {fixture: participantsFixturePath}
        ).as('getParticipants')

        // Cliquer sur le bouton pour gérer une épreuve
        cy.get('button').contains('Modifier participants').first().click();
        cy.wait("@getParticipants");
        cy.wait(500);

        cy.contains('button', 'Forfait').first().click();
        cy.wait(500);

        // Vérifier que la modal de confirmation s'affiche
        cy.get('.modal').should('be.visible');
        cy.contains('Confirmer le forfait').should('be.visible');

        const getAllNewParticipantsFixturePath = 'gestion-participants/allParticipantsWithForfeit.json'


        cy.intercept('POST','**/commissaire/trials/1/forfeit',
            {statusCode : 200}
        ).as('forfeit')

        cy.intercept('GET','**/commissaire/trials/1/participants/full',
            {fixture : getAllNewParticipantsFixturePath}
        ).as('getAllNewParticipants')

        // Cliquer sur "Confirmer le forfait"
        cy.get('.modal').within(() => {
            cy.contains('button', 'Confirmer le forfait').click();
        });
        cy.wait('@forfeit');
        cy.wait('@getAllNewParticipants');
        cy.wait(500);

        // Vérifier que la modal se ferme et le badge Forfait apparaît
        cy.get('.modal').should('not.exist');
        cy.get('.badge').contains('Forfait').should('be.visible');
    });

    it('Scénario : Annuler le forfait d\'un participant', () => {

        const participantsFixturePath = 'gestion-participants/allParticipantsWithForfeit.json'
        const newParticipantsFixturePath = 'gestion-participants/allParticipants.json'

        cy.intercept('GET','**/commissaire/trials/1/participants/full',
            {fixture: participantsFixturePath}
        ).as('getParticipants')

        // Cliquer sur le bouton pour gérer une épreuve
        cy.get('button').contains('Modifier participants').first().click();
        cy.wait('@getParticipants');
        cy.wait(500);

        cy.intercept('POST','**/commissaire/trials/1/unforfeit',
            {statusCode : 200}
        ).as('PasForfeit')

        cy.intercept('GET','**/commissaire/trials/1/participants/full',
            {fixture: newParticipantsFixturePath}
        ).as('getNewParticipants')
        cy.contains('button', 'Annuler forfait').first().click();
        cy.wait('@PasForfeit');
        cy.wait('@getNewParticipants');
        cy.wait(500);

        // Vérifier que le bouton "Forfait" réapparaît pour ce participant
        cy.contains('button', 'Forfait').should('be.visible');
    });

    it('Scénario : Retirer un participant via bouton ✕', () => {
        const participantsFixturePath = 'gestion-participants/addingParticipantToAllParticipants.json'
        const newParticipantsFixturePath = 'gestion-participants/removeOneParticipant.json'
        cy.intercept('GET','**/commissaire/trials/1/participants/full',
            {fixture: participantsFixturePath}
        ).as('getParticipants')

        // Cliquer sur le bouton pour gérer une épreuve
        cy.get('button').contains('Modifier participants').first().click();
        cy.wait("@getParticipants");
        cy.wait(500);

        cy.contains('button', '✕').first().click();
        cy.wait(500);

        // Vérifier que la modal de retrait s'affiche
        cy.get('.modal').should('be.visible');
        cy.contains('Retirer le participant').should('be.visible');

        cy.intercept('DELETE','**/commissaire/trials/1/participants?participantId=1&participantType=TEAM',
            {statusCode : 200}
        ).as('deleteFromTeam')

        cy.intercept('GET','**/commissaire/trials/1/participants/full',
            {fixture: newParticipantsFixturePath}
        ).as('getNewParticipants')

        // Cliquer sur "Retirer le participant"
        cy.get('.modal').within(() => {
            cy.contains('button', 'Retirer le participant').click();
        });
        cy.wait('@deleteFromTeam');
        cy.wait('@getNewParticipants');
        cy.wait(500);

        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');

        cy.fixture(newParticipantsFixturePath).then((trial) => {
            for (const participant of trial.potentialTeams) {
                cy.contains('Équipes potentielles')
                    .parent()
                    .get(".card-body")
                    .contains(participant.name).should('be.visible')
                    .parent()
                    .contains(participant.country).should('be.visible')
            }
        })
    });

    it('Scénario : Annuler l\'ajout d\'un participant via la modal', () => {
        const participantsFixturePath = 'gestion-participants/addingParticipantToAllParticipants.json'
        cy.intercept('GET','**/commissaire/trials/1/participants/full',
            {fixture: participantsFixturePath}
        ).as('getParticipants')

        // Cliquer sur le bouton pour gérer une épreuve
        cy.get('button').contains('Modifier participants').first().click();
        cy.wait("@getParticipants");
        cy.wait(500);

        cy.contains('button', '+ Ajouter').first().click();
        cy.wait(500);

        // Vérifier que la modal s'affiche
        cy.get('.modal').should('be.visible');

        // Cliquer sur "Annuler"
        cy.get('.modal').within(() => {
            cy.contains('button', 'Annuler').click();
        });
        cy.wait(500);

        // Vérifier que la modal se ferme sans ajouter
        cy.get('.modal').should('not.exist');
    });


    it('Scénario : Bouton Retour vers les épreuves', () => {
        const participantsFixturePath = 'gestion-participants/addingParticipantToAllParticipants.json'
        cy.intercept('GET','**/commissaire/trials/1/participants/full',
            {fixture: participantsFixturePath}
        ).as('getParticipants')

        // Cliquer sur le bouton pour gérer une épreuve
        cy.get('button').contains('Modifier participants').first().click();
        cy.wait("@getParticipants");
        cy.wait(500);

        // Vérifier que le bouton "Retour aux épreuves" existe
        cy.contains('button', '← Retour aux épreuves').should('be.visible');

        // Cliquer sur le bouton
        cy.contains('button', '← Retour aux épreuves').click();
        cy.wait(500);

        // Vérifier que la navigation a eu lieu
        cy.url().should('not.contain', '/participants');
    });

    it('Scénario : État vide - Aucun participant potentiel', () => {
        const participantsFixturePath = 'gestion-participants/voidParticipants.json'
        cy.intercept('GET','**/commissaire/trials/1/participants/full',
            {fixture: participantsFixturePath}
        ).as('getParticipants')

        // Cliquer sur le bouton pour gérer une épreuve
        cy.get('button').contains('Modifier participants').first().click();
        cy.wait("@getParticipants");
        cy.wait(500);

        cy.contains('🏃 Solo').should('be.visible');
        cy.contains('👥 Équipe').should('be.visible');
        cy.contains('Participants inscrits').should('be.visible');
        cy.contains('Aucun participant inscrit').should('be.visible');
        cy.contains('Aucun équipe disponible').should('be.visible');
    });
});
