describe('Tests - Gestion des Équipes', () => {
    const teamFixturePath = "gestion-equipes/teams.json"
    const countryFixturePath = "gestion-equipes/countries.json"
    const athleteFixturePath = "gestion-equipes/athletes.json"

    beforeEach(() => {
        // Désactive l'arrêt du test sur les exceptions non gérées
        cy.on('uncaught:exception', () => false);
        
        // Visite la page d'accueil
        cy.visit('/');
        cy.wait(1500);
        
        // Connexion en tant que commissaire
        cy.contains('Connexion').click();
        cy.wait(1500);
        
        // Remplir le formulaire de connexion
        cy.get('input[type="email"]').type('commissaire@example.com');
        cy.get('input[type="password"]').type('test123');

        // Soumettre le formulaire
        cy.get('button[type="submit"]').click();
        cy.wait(1000);

        cy.intercept('GET', `**/commissaire/teams`,
            { fixture: teamFixturePath}
        ).as('getTeams');

        cy.intercept('GET', `**/countries`,
            { fixture: countryFixturePath}
        ).as('getCountries');

        cy.intercept('GET','**/commissaire/users?role=ATHLETE',
            {fixture: athleteFixturePath}
        ).as('getAthletes')

        // Naviguer vers la page de gestion des équipes
        cy.contains('Gestion équipes').click();

        cy.wait('@getTeams');
        cy.wait('@getCountries');
        cy.wait('@getAthletes');
        cy.wait(500);
    });

    it('Scénario : Affichage de la page de gestion des équipes', () => {
        // Vérifier que la page se charge avec le titre
        cy.contains('Gestion des Équipes').should('be.visible');

        // Vérifier que le bouton de création existe
        cy.contains('button', 'Créer une équipe').should('be.visible');

        // Vérifier que le tableau des équipes est visible
        cy.get('table').should('be.visible');
        cy.contains('th', 'Nom').should('be.visible');
        cy.contains('th', 'Pays').should('be.visible');
        cy.contains('th', 'Membres').should('be.visible');
        cy.contains('th', 'Actions').should('be.visible');
    });

    it('Scénario : Afficher la liste des équipes existantes', () => {
        cy.fixture(teamFixturePath).then((teams) => {
            for (const team of teams) {
                cy.contains(team.name).should('be.visible');
                cy.contains(team.countryCode).should('be.visible');
                for (const member of team.members) {
                    cy.contains(`${member.name} ${member.lastname}`).should('be.visible');
                }
            }
        })

    });

    it('Scénario : Ouvrir la modal de création d\'équipe', () => {
        // Cliquer sur le bouton de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(500);
        // Vérifier que la modal s'affiche
        cy.get('.modal').should('be.visible');
        cy.get('.modal-title').should('contain', 'Créer une nouvelle équipe');

        // Vérifier les champs du formulaire
        cy.get('.modal').within(() => {
            cy.get('input[name="name"]').should('be.visible');
            cy.get('select[name="countryCode"]').should('be.visible');
            cy.contains('Membres (Athlètes)').should('be.visible');
        });

        // Fermer la modal
        cy.contains('button', 'Annuler').click();
        cy.wait(500);
        cy.get('.modal').should('not.exist');
    });

    it('Scénario : Créer une équipe sans membres', () => {
        // Ouvrir la modal de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(1000);

        // Remplir le formulaire
        cy.get('.modal').within(() => {
            cy.get('input[name="name"]').type('Nouvelle Équipe');
            cy.get('select[name="countryCode"]').select("FR");

            cy.intercept('POST','**/commissaire/teams',
                {statusCode: 201}
            ).as('createTeam')

            const newTeamFixturePath = 'gestion-equipes/teamsWithNewTeam.json'

            cy.intercept('GET', `**/commissaire/teams`,
                { fixture: newTeamFixturePath}
            ).as('getNewTeams');

            // Soumettre sans sélectionner de membres
            cy.contains('button', 'Créer l\'équipe').click();
        });
        cy.wait('@createTeam');
        cy.wait('@getNewTeams');
        cy.wait(500);

        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');
        cy.wait(1000);

        // Vérifier que la nouvelle équipe apparaît dans le tableau
        cy.contains('Nouvelle Équipe').should('be.visible');
    });

    it('Scénario : Créer une équipe avec sélection d\'athlètes via checkboxes', () => {
        // Ouvrir la modal de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(1000);

        const newTeamFixturePath = 'gestion-equipes/teamsNewTeamAndMembers.json'

        // Remplir le formulaire
        cy.get('.modal').within(() => {
            cy.get('input[name="name"]').type('Nouvelle Équipe');
            cy.get('select[name="countryCode"]').select('FR');

            // Vérifier que des checkboxes sont disponibles
            cy.get('input[type="checkbox"]').should('have.length.greaterThan', 0);

            // Sélectionner le premier et le deuxième athlète
            cy.get('input[type="checkbox"]').first().check();
            cy.get('input[type="checkbox"]').eq(1).check();

            // Vérifier que les checkboxes sont cochées
            cy.get('input[type="checkbox"]').first().should('be.checked');
            cy.get('input[type="checkbox"]').eq(1).should('be.checked');

            cy.intercept('POST','**/commissaire/teams',
                {statusCode: 201}
            ).as('createTeam')


            cy.intercept('GET', `**/commissaire/teams`,
                { fixture: newTeamFixturePath}
            ).as('getNewTeams');

            // Soumettre le formulaire
            cy.contains('button', 'Créer l\'équipe').click();
        });

        cy.wait('@createTeam');
        cy.wait('@getNewTeams');
        cy.wait(500);

        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');

        // Vérifier que la nouvelle équipe apparaît avec ses membres
        cy.fixture(newTeamFixturePath).then((teams) => {
            for (const team of teams) {
                cy.contains(team.name).should('be.visible');
                cy.contains(team.countryCode).should('be.visible');
                for (const member of team.members) {
                    cy.contains(`${member.name} ${member.lastname}`).should('be.visible');
                }
            }
        })

    });

    it('Scénario : Cocher/décocher des athlètes dynamiquement', () => {
        // Ouvrir la modal de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(500);

        cy.get('.modal').within(() => {
            // Cocher le premier athlète
            cy.get('input[type="checkbox"]').first().check();
            cy.get('input[type="checkbox"]').first().should('be.checked');
            cy.wait(100);

            // Décocher le premier athlète
            cy.get('input[type="checkbox"]').first().uncheck();
            cy.get('input[type="checkbox"]').first().should('not.be.checked');
         });

    });

    it('Scénario : Validation - Créer une équipe sans nom', () => {
        // Ouvrir la modal de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(500);

        cy.get('.modal').within(() => {
            // Ne pas remplir le nom
            cy.get('select[name="countryCode"]').select('FR');
            cy.wait(500);

            // Tenter de soumettre
            cy.contains('button', 'Créer l\'équipe').click();
            cy.wait(500);

            // Vérifier que le message d'erreur s'affiche
            cy.get('.alert-danger').should('be.visible');
            cy.contains('Le nom de l\'équipe est obligatoire').should('be.visible');
        });
    });

    it('Scénario : Validation - Créer une équipe sans pays', () => {
        // Ouvrir la modal de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(500);

        cy.get('.modal').within(() => {
            // Remplir seulement le nom
            cy.get('input[name="name"]').type('Équipe sans pays');
            cy.wait(500);

            // Tenter de soumettre sans sélectionner de pays
            cy.contains('button', 'Créer l\'équipe').click();
            cy.wait(500);

            // Vérifier que le message d'erreur s'affiche
            cy.get('.alert-danger').should('be.visible');
            cy.contains('Le pays est obligatoire').should('be.visible');
        });
    });

    it('Scénario : Ouvrir la modal de modification d\'équipe', () => {
        // Cliquer sur le bouton de modification de la première équipe
        cy.get('table tbody tr').first().within(() => {
            cy.contains('button', 'Modifier').click();
        });
        cy.wait(500);

        // Vérifier que la modal s'affiche
        cy.get('.modal').should('be.visible');
        cy.get('.modal-title').should('contain', 'Modifier l\'équipe');

        cy.fixture(teamFixturePath).then((teams) => {
            const firstTeam = teams[0]

            // Vérifier que les champs sont pré-remplis
            cy.get('.modal').within(() => {
                cy.get('input[name="name"]').should('have.value', firstTeam.name);
                cy.get('select[name="countryCode"]').should('have.value', firstTeam.countryCode);
            });
        })

        // Fermer la modal
        cy.contains('button', 'Annuler').click();
        cy.wait(500);
        cy.get('.modal').should('not.exist');
    });

    it('Scénario : Modifier le nom d\'une équipe', () => {
        // Cliquer sur le bouton de modification de Team A
        cy.contains('Team A').parents('tr').within(() => {
            cy.contains('button', 'Modifier').click();
        });
        cy.wait(500);

        const fixturePath = "gestion-equipes/teamsWithUpdatedName.json"

        cy.get('.modal').within(() => {
            // Modifier le nom
            cy.get('input[name="name"]').clear();
            cy.get('input[name="name"]').type('J\'adore le bleu');

            cy.intercept('PUT','**/commissaire/teams/1',
                {statusCode: 200}
            ).as('updateTeam')

            cy.intercept('GET', `**/commissaire/teams`,
                { fixture: fixturePath}
            ).as('getNewTeams');

            // Soumettre
            cy.contains('button', 'Mettre à jour').click();
        });
        cy.wait("@updateTeam");
        cy.wait("@getNewTeams");
        cy.wait(500);

        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');

        // Vérifier que le nom a été modifié dans le tableau
        cy.contains('J\'adore le bleu').should('be.visible');
    });

    it('Scénario : Ajouter des membres à une équipe existante', () => {
        cy.contains('Team A').parents('tr').within(() => {
            cy.contains('button', 'Modifier').click();
        });
        cy.wait(500);

        const fixturePath = "gestion-equipes/teamsWithUpdatedMembers.json"

        cy.get('.modal').within(() => {
            // Sélectionner des athlètes
            cy.get('input[type="checkbox"]').first().check();

            cy.intercept('PUT','**/commissaire/teams/1',
                {statusCode: 200}
            ).as('updateTeam')

            cy.intercept('GET', `**/commissaire/teams`,
                { fixture: fixturePath}
            ).as('getNewTeams');

            // Soumettre
            cy.contains('button', 'Mettre à jour').click();
        });
        cy.wait("@updateTeam");
        cy.wait("@getNewTeams");
        cy.wait(500);

        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');

        // Vérifier que les membres s'affichent
        cy.fixture(fixturePath).then((teams) => {
            for (const team of teams) {
                cy.contains(team.name).should('be.visible');
                cy.contains(team.countryCode).should('be.visible');
                for (const member of team.members) {
                    cy.contains(`${member.name} ${member.lastname}`).should('be.visible');
                }
            }
        })
    });

    it('Scénario : Supprimer une équipe', () => {
        cy.contains('Team A').parents('tr').within(() => {
            cy.contains('button', 'Supprimer').click();
        });
        cy.wait(500);

        const fixturePath = "gestion-equipes/teamsWithoutTeamA.json"

        cy.get('.modal').within(() => {
            cy.intercept('DELETE','**/commissaire/teams/1',
                {statusCode: 200}
            ).as('deleteTeam')

            cy.intercept('GET', `**/commissaire/teams`,
                { fixture: fixturePath}
            ).as('getNewTeams');

            // Soumettre
            cy.contains('button', 'Supprimer').click();
        });
        cy.wait("@deleteTeam");
        cy.wait("@getNewTeams");
        cy.wait(500);

        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');

        // Vérifier que les membres s'affichent
        cy.fixture(fixturePath).then((teams) => {
            for (const team of teams) {
                cy.contains(team.name).should('be.visible');
                cy.contains(team.countryCode).should('be.visible');
                for (const member of team.members) {
                    cy.contains(`${member.name} ${member.lastname}`).should('be.visible');
                }
            }
        })

    });

    it('Scénario : Annuler la suppression d\'une équipe', () => {
        cy.contains('Team A').parents('tr').within(() => {
            cy.contains('button', 'Supprimer').click();
        });
        cy.wait(500);

        cy.get('.modal').within(() => {
            // Pas Soumettre
            cy.contains('button', 'Annuler').click();
        });
        cy.wait(500);

        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');

        // Vérifier que les membres s'affichent
        cy.contains('Team A').should('be.visible')
    });

});
