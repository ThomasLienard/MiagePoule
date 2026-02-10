describe('Tests - Gestion des Participants aux Épreuves', () => {

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
        cy.get('input[type="email"]').type('commissaire@test.com');
        cy.wait(500);
        cy.get('input[type="password"]').type('test123');
        cy.wait(500);
        
        // Soumettre le formulaire
        cy.get('button[type="submit"]').click();
        cy.wait(3000);
        
        // Naviguer vers la page de gestion des participants
        cy.visit('/commissaire/trials');
        cy.wait(3000);
        
        // Attendre que le spinner de chargement disparaisse
        cy.get('.spinner-border', { timeout: 10000 }).should('not.exist');
        cy.wait(1000);
        
        // Cliquer sur le bouton pour gérer une épreuve
        cy.get('button').contains('Modifier participants').first().click();
        cy.wait(3000);
        
        // Attendre que la page de gestion charge
        cy.get('.spinner-border', { timeout: 10000 }).should('not.exist');
        cy.wait(1000);
    });

    it('Scénario : Affichage de la page de gestion des participants', () => {
        // Vérifier que la page se charge avec le titre
        cy.contains('Ajouter un participant').should('be.visible');
        // Vérifier que la section potentiels existe (peut être "Participants" ou "Équipes")
        cy.contains(/(Participants potentiels|Équipes potentielles)/).should('be.visible');
        cy.contains('Participants inscrits').should('be.visible');
        cy.wait(1000);
    });

    it('Scénario : Afficher les sections - Participants potentiels et inscrits', () => {
        // Vérifier que les deux colonnes s'affichent (texte varie selon mode solo/équipe)
        cy.contains(/(Participants potentiels|Équipes potentielles)/).should('be.visible');
        cy.contains('Participants inscrits').should('be.visible');
        cy.wait(1000);
    });

    it('Scénario : Afficher le badge du mode d\'épreuve', () => {
        // Vérifier que le badge indique le type d'épreuve
        cy.get('.badge').should('contain.text', 'Épreuve');
        cy.wait(1000);
    });

    it('Scénario : Ajouter un participant via bouton "+ Ajouter"', () => {
        // Il y a des équipes potentielles disponibles (Team C, Team D)
        // Trouver la première équipe potentielle et cliquer sur "+ Ajouter"
        cy.contains('button', '+ Ajouter').first().click();
        cy.wait(2000);
        
        // Vérifier que la modal de détails s'affiche
        cy.get('.modal').should('be.visible');
        cy.contains('Détails').should('be.visible');
        cy.wait(1000);
        
        // Cliquer sur "Inscrire le participant"
        cy.get('.modal').within(() => {
            cy.contains('button', 'Inscrire le participant').click();
        });
        cy.wait(2000);
        
        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');
        cy.wait(1000);
    });

    it('Scénario : Déclarer un participant en forfait', () => {
        // Il y a des participants inscrits non forfait (Team A)
        // Trouver un participant inscrit et cliquer sur "Forfait"
        cy.contains('button', 'Forfait').first().click();
        cy.wait(2000);
        
        // Vérifier que la modal de confirmation s'affiche
        cy.get('.modal').should('be.visible');
        cy.contains('Confirmer le forfait').should('be.visible');
        cy.wait(1000);
        
        // Cliquer sur "Confirmer le forfait"
        cy.get('.modal').within(() => {
            cy.contains('button', 'Confirmer le forfait').click();
        });
        cy.wait(2000);
        
        // Vérifier que la modal se ferme et le badge Forfait apparaît
        cy.get('.modal').should('not.exist');
        cy.get('.badge').contains('Forfait').should('be.visible');
        cy.wait(1000);
    });

    it('Scénario : Annuler le forfait d\'un participant', () => {
        // Team B est en forfait sur Trial 1 (ou Team A après le test précédent)
        // Trouver le participant avec le badge Forfait et cliquer sur "Annuler forfait"
        cy.contains('button', 'Annuler forfait').first().click();
        cy.wait(2000);
        
        // Vérifier que le bouton "Forfait" réapparaît pour ce participant
        cy.contains('button', 'Forfait').should('be.visible');
        cy.wait(1000);
    });

    it('Scénario : Retirer un participant via bouton ✕', () => {
        // Il y a des participants inscrits avec le bouton ✕
        // Trouver le premier participant inscrit et cliquer sur le bouton ✕ (retirer)
        cy.contains('button', '✕').first().click();
        cy.wait(2000);
        
        // Vérifier que la modal de retrait s'affiche
        cy.get('.modal').should('be.visible');
        cy.contains('Retirer le participant').should('be.visible');
        cy.wait(1000);
        
        // Cliquer sur "Retirer le participant"
        cy.get('.modal').within(() => {
            cy.contains('button', 'Retirer le participant').click();
        });
        cy.wait(2000);
        
        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');
        cy.wait(1000);
    });

    it('Scénario : Interaction du drag and drop - Zone d\'accueil active', () => {
        // Vérifier que l\'instruction de drag and drop est visible
        cy.contains('Glisser-déposer').should('be.visible');
        cy.contains('ou utilisez les boutons').should('be.visible');
        cy.wait(1000);
    });

    it('Scénario : Afficher des détails du participant dans la modal', () => {
        // Il y a des équipes potentielles (Team C, Team D)
        // Cliquer sur "+ Ajouter" pour ouvrir la modal
        cy.contains('button', '+ Ajouter').first().click();
        cy.wait(2000);
        
        // Vérifier les informations affichées dans la modal
        cy.get('.modal').should('be.visible');
        cy.contains('Nom :').should('be.visible');
        cy.contains('Type :').should('be.visible');
        cy.wait(1000);
        
        // Fermer la modal
        cy.get('.modal').within(() => {
            cy.contains('button', 'Annuler').click();
        });
        cy.wait(1000);
    });

    it('Scénario : Annuler l\'ajout d\'un participant via la modal', () => {
        // Il y a des équipes potentielles (Team C, Team D)
        // Cliquer sur "+ Ajouter"
        cy.contains('button', '+ Ajouter').first().click();
        cy.wait(2000);
        
        // Vérifier que la modal s'affiche
        cy.get('.modal').should('be.visible');
        
        // Cliquer sur "Annuler"
        cy.get('.modal').within(() => {
            cy.contains('button', 'Annuler').click();
        });
        cy.wait(1000);
        
        // Vérifier que la modal se ferme sans ajouter
        cy.get('.modal').should('not.exist');
        cy.wait(1000);
    });

    

    it('Scénario : Bouton Retour vers les épreuves', () => {
        // Vérifier que le bouton "Retour aux épreuves" existe
        cy.contains('button', '← Retour aux épreuves').should('be.visible');
        
        // Cliquer sur le bouton
        cy.contains('button', '← Retour aux épreuves').click();
        cy.wait(2000);
        
        // Vérifier que la navigation a eu lieu
        cy.url().should('not.contain', '/participants');
        cy.wait(1000);
    });

    it('Scénario : État vide - Aucun participant potentiel', () => {
        // Avec les données de test, il y a des équipes potentielles (Team C, Team D)
        // Donc cette section ne doit pas afficher de message vide
        cy.contains('Équipes potentielles').should('be.visible');
        cy.contains('Aucune équipe disponible').should('not.exist');
        cy.wait(1000);
    });

    it('Scénario : État vide - Aucun participant inscrit', () => {
        // Avec les données de test, il y a des participants inscrits (Team A, Team B)
        // Donc cette section ne doit pas afficher de message vide
        cy.contains('Participants inscrits').should('be.visible');
        cy.contains('Aucun participant inscrit').should('not.exist');
        cy.wait(1000);
    });

    it('Scénario : Badge "Glisser-déposer" apparaît en plein centre', () => {
        // Vérifier que le badge avec les instructions est visible
        cy.get('.badge').contains('↔️').should('be.visible');
        cy.wait(1000);
    });

    it('Scénario : Informations du pays s\'affichent', () => {
        // Les équipes ont des pays définis (Team A: FR, Team B: US, etc.)
        // Vérifier que les informations du pays (🌍 emoji) s'affichent
        cy.contains('🌍').should('exist');
        cy.wait(1000);
    });

});
