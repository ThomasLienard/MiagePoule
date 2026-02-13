describe('Tests - Gestion des Équipes', () => {

    beforeEach(() => {
        // Désactive l'arrêt du test sur les exceptions non gérées
        cy.on('uncaught:exception', () => false);
        
        // Visite la page d'accueil
        cy.visit('/');
        cy.wait(2000);
        
        // Connexion en tant que commissaire
        cy.contains('Connexion').click();
        cy.wait(2000);
        
        // Remplir le formulaire de connexion
        cy.get('input[type="email"]').type('commissaire@example.com');
        cy.wait(500);
        cy.get('input[type="password"]').type('test123');
        cy.wait(500);
        
        // Soumettre le formulaire
        cy.get('button[type="submit"]').click();
        cy.wait(3000);
        
        // Naviguer vers la page de gestion des équipes
        cy.contains('Gestion équipes').click();
        cy.wait(3000);
        
        // Attendre que le spinner de chargement disparaisse
        cy.get('.spinner-border', { timeout: 10000 }).should('not.exist');
        cy.wait(1000);
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
        cy.wait(1000);
    });

    it('Scénario : Afficher la liste des équipes existantes', () => {
        // Vérifier que les équipes du data.sql s'affichent
        cy.contains('Team A').should('be.visible');
        cy.contains('Team B').should('be.visible');
        cy.contains('Team C').should('be.visible');
        cy.contains('Team D').should('be.visible');
        
        // Vérifier les badges de pays
        cy.get('table tbody tr').first().within(() => {
            cy.get('.badge').should('exist');
        });
        cy.wait(1000);
    });

    it('Scénario : Ouvrir la modal de création d\'équipe', () => {
        // Cliquer sur le bouton de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(1000);
        
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
        cy.wait(1000);
        cy.get('.modal').should('not.exist');
    });

    it('Scénario : Créer une équipe sans membres', () => {
        // Ouvrir la modal de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(1000);
        
        // Remplir le formulaire
        cy.get('.modal').within(() => {
            cy.get('input[name="name"]').type('Nouvelle Équipe Test');
            cy.wait(500);
            cy.get('select[name="countryCode"]').select('FR');
            cy.wait(500);
            
            // Soumettre sans sélectionner de membres
            cy.contains('button', 'Créer l\'équipe').click();
        });
        cy.wait(2000);
        
        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');
        cy.wait(1000);
        
        // Vérifier que la nouvelle équipe apparaît dans le tableau
        cy.contains('Nouvelle Équipe Test').should('be.visible');
        cy.wait(1000);
    });

    it('Scénario : Créer une équipe avec sélection d\'athlètes via checkboxes', () => {
        // Ouvrir la modal de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(1000);
        
        // Remplir le formulaire
        cy.get('.modal').within(() => {
            cy.get('input[name="name"]').type('Équipe avec Membres');
            cy.wait(500);
            cy.get('select[name="countryCode"]').select('US');
            cy.wait(500);
            
            // Vérifier que des checkboxes sont disponibles
            cy.get('input[type="checkbox"]').should('have.length.greaterThan', 0);
            
            // Sélectionner le premier et le deuxième athlète
            cy.get('input[type="checkbox"]').first().check();
            cy.wait(500);
            cy.get('input[type="checkbox"]').eq(1).check();
            cy.wait(500);
            
            // Vérifier que les checkboxes sont cochées
            cy.get('input[type="checkbox"]').first().should('be.checked');
            cy.get('input[type="checkbox"]').eq(1).should('be.checked');
            
            // Soumettre le formulaire
            cy.contains('button', 'Créer l\'équipe').click();
        });
        cy.wait(2000);
        
        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');
        cy.wait(1000);
        
        // Vérifier que la nouvelle équipe apparaît avec ses membres
        cy.contains('Équipe avec Membres').should('be.visible');
        cy.wait(1000);
    });

    it('Scénario : Cocher/décocher des athlètes dynamiquement', () => {
        // Ouvrir la modal de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(1000);
        
        cy.get('.modal').within(() => {
            // Cocher le premier athlète
            cy.get('input[type="checkbox"]').first().check();
            cy.wait(500);
            cy.get('input[type="checkbox"]').first().should('be.checked');
            
            // Décocher le premier athlète
            cy.get('input[type="checkbox"]').first().uncheck();
            cy.wait(500);
            cy.get('input[type="checkbox"]').first().should('not.be.checked');
            
            // Cocher plusieurs athlètes
            cy.get('input[type="checkbox"]').eq(0).check();
            cy.get('input[type="checkbox"]').eq(1).check();
            cy.wait(500);
            
            // Vérifier qu'ils sont tous cochés
            cy.get('input[type="checkbox"]').eq(0).should('be.checked');
            cy.get('input[type="checkbox"]').eq(1).should('be.checked');
        });
        
        // Fermer la modal sans créer
        cy.contains('button', 'Annuler').click();
        cy.wait(1000);
    });

    it('Scénario : Validation - Créer une équipe sans nom', () => {
        // Ouvrir la modal de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(1000);
        
        cy.get('.modal').within(() => {
            // Ne pas remplir le nom
            cy.get('select[name="countryCode"]').select('FR');
            cy.wait(500);
            
            // Tenter de soumettre
            cy.contains('button', 'Créer l\'équipe').click();
            cy.wait(1000);
            
            // Vérifier que le message d'erreur s'affiche
            cy.get('.alert-danger').should('be.visible');
            cy.contains('Le nom de l\'équipe est obligatoire').should('be.visible');
        });
        cy.wait(1000);
    });

    it('Scénario : Validation - Créer une équipe sans pays', () => {
        // Ouvrir la modal de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(1000);
        
        cy.get('.modal').within(() => {
            // Remplir seulement le nom
            cy.get('input[name="name"]').type('Équipe sans pays');
            cy.wait(500);
            
            // Tenter de soumettre sans sélectionner de pays
            cy.contains('button', 'Créer l\'équipe').click();
            cy.wait(1000);
            
            // Vérifier que le message d'erreur s'affiche
            cy.get('.alert-danger').should('be.visible');
            cy.contains('Le pays est obligatoire').should('be.visible');
        });
        cy.wait(1000);
    });

    it('Scénario : Ouvrir la modal de modification d\'équipe', () => {
        // Cliquer sur le bouton de modification de la première équipe
        cy.get('table tbody tr').first().within(() => {
            cy.contains('button', 'Modifier').click();
        });
        cy.wait(1000);
        
        // Vérifier que la modal s'affiche
        cy.get('.modal').should('be.visible');
        cy.get('.modal-title').should('contain', 'Modifier l\'équipe');
        
        // Vérifier que les champs sont pré-remplis
        cy.get('.modal').within(() => {
            cy.get('input[name="name"]').should('not.have.value', '');
            cy.get('select[name="countryCode"]').should('not.have.value', '');
        });
        
        // Fermer la modal
        cy.contains('button', 'Annuler').click();
        cy.wait(1000);
        cy.get('.modal').should('not.exist');
    });

    it('Scénario : Modifier le nom d\'une équipe', () => {
        // Cliquer sur le bouton de modification de Team A
        cy.contains('Team A').parents('tr').within(() => {
            cy.contains('button', 'Modifier').click();
        });
        cy.wait(1000);
        
        cy.get('.modal').within(() => {
            // Modifier le nom
            cy.get('input[name="name"]').clear();
            cy.wait(500);
            cy.get('input[name="name"]').type('Team A Modifié');
            cy.wait(500);
            
            // Soumettre
            cy.contains('button', 'Mettre à jour').click();
        });
        cy.wait(2000);
        
        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');
        cy.wait(1000);
        
        // Vérifier que le nom a été modifié dans le tableau
        cy.contains('Team A Modifié').should('be.visible');
        cy.wait(1000);
    });

    it('Scénario : Ajouter des membres à une équipe existante', () => {
        // Modifier Team C qui n'a pas de membres
        cy.contains('Team C').parents('tr').within(() => {
            cy.contains('button', 'Modifier').click();
        });
        cy.wait(1000);
        
        cy.get('.modal').within(() => {
            // Sélectionner des athlètes
            cy.get('input[type="checkbox"]').first().check();
            cy.wait(500);
            cy.get('input[type="checkbox"]').eq(1).check();
            cy.wait(500);
            
            // Soumettre
            cy.contains('button', 'Mettre à jour').click();
        });
        cy.wait(2000);
        
        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');
        cy.wait(1000);
        
        // Vérifier que les membres s'affichent
        cy.contains('Team C').parents('tr').within(() => {
            cy.get('td').eq(2).should('not.contain', 'Aucun membre');
        });
        cy.wait(1000);
    });


    it('Scénario : Supprimer une équipe', () => {
        // Compter le nombre d'équipes avant suppression
        cy.get('table tbody tr').then((rows) => {
            const initialCount = rows.length;
            
            // Cliquer sur le bouton de suppression de la première équipe
            cy.get('table tbody tr').first().within(() => {
                cy.contains('button', 'Supprimer').click();
            });
            cy.wait(1000);
            
            // Vérifier que la modal de confirmation s'affiche
            cy.get('.modal').should('be.visible');
            cy.contains('Confirmer la suppression').should('be.visible');
            cy.wait(500);
            
            // Confirmer la suppression
            cy.get('.modal').within(() => {
                cy.contains('button', 'Supprimer').click();
            });
            cy.wait(2000);
            
            // Vérifier que la modal se ferme
            cy.get('.modal').should('not.exist');
            cy.wait(1000);
            
            // Vérifier que le nombre d'équipes a diminué
            cy.get('table tbody tr').should('have.length', initialCount - 1);
        });
        cy.wait(1000);
    });

    it('Scénario : Annuler la suppression d\'une équipe', () => {
        // Compter le nombre d'équipes avant
        cy.get('table tbody tr').then((rows) => {
            const initialCount = rows.length;
            
            // Cliquer sur le bouton de suppression
            cy.get('table tbody tr').first().within(() => {
                cy.contains('button', 'Supprimer').click();
            });
            cy.wait(1000);
            
            // Vérifier que la modal de confirmation s'affiche
            cy.get('.modal').should('be.visible');
            cy.wait(500);
            
            // Annuler la suppression
            cy.get('.modal').within(() => {
                cy.contains('button', 'Annuler').click();
            });
            cy.wait(1000);
            
            // Vérifier que la modal se ferme
            cy.get('.modal').should('not.exist');
            cy.wait(500);
            
            // Vérifier que le nombre d'équipes n'a pas changé
            cy.get('table tbody tr').should('have.length', initialCount);
        });
        cy.wait(1000);
    });

    it('Scénario : Afficher correctement les noms des athlètes', () => {
        // Vérifier que les noms s'affichent au format "lastname name"
        cy.get('table tbody tr').first().within(() => {
            cy.get('td').eq(2).should('not.contain', 'ID:');
        });
        cy.wait(1000);
    });

    it('Scénario : Vérifier le texte d\'aide pour les checkboxes', () => {
        // Ouvrir la modal de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(1000);
        
        cy.get('.modal').within(() => {
            // Vérifier que le texte d'aide s'affiche
            cy.contains('Cliquez sur un athlète pour l\'ajouter ou le retirer de l\'équipe')
                .should('be.visible');
        });
        
        // Fermer la modal
        cy.contains('button', 'Annuler').click();
        cy.wait(1000);
    });

    it('Scénario : Navigation - Retour à la liste après fermeture de modal', () => {
        // Ouvrir et fermer la modal de création
        cy.contains('button', 'Créer une équipe').click();
        cy.wait(1000);
        cy.contains('button', 'Annuler').click();
        cy.wait(1000);
        
        // Vérifier qu'on est toujours sur la page de gestion
        cy.contains('Gestion des Équipes').should('be.visible');
        cy.get('table').should('be.visible');
        cy.wait(1000);
    });

});
