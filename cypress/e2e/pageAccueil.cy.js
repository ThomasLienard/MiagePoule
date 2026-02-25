describe('Tests - CiblOrgaSport (Mode Ralenti)', () => {

    beforeEach(() => {
        // Empêche le test de planter si React jette une erreur mineure
        cy.on('uncaught:exception', () => false);

        cy.visit('/', {
            onBeforeLoad(win) {
                // Espion pour l'itinéraire
                cy.stub(win, 'open').as('windowOpen');
            }
        });
    });

    it('Scénario complet : Recherche, Carte et Détails', () => {
        //FIXTURE TODO
        cy.get('input[placeholder*="Rechercher"]').type('100m');
        cy.wait(3000); // On observe les résultats filtrés dans la liste
        

        cy.contains('.card-title', '100m Trial Final').click();
        cy.wait(3000); // On observe la carte se déplacer et la bulle s'ouvrir

        // 3. Bulle Google Maps
        cy.get('.gm-style-iw', { timeout: 10000 }).within(() => {
            cy.contains('100m Trial Final').should('exist');
            cy.wait(2000);

            // Test Itinéraire
            cy.contains('button', 'Itinéraire').click({ force: true });
            cy.get('@windowOpen').should('be.called');
            cy.wait(3000); // On vérifie visuellement que l'action est faite

            // Test Détails
            cy.contains('button', 'Détails').click({ force: true });
        });

        // 4. Page de Détails
        cy.wait(3000); // On admire la page de détails
        cy.url().should('match', /\/public\/(events|trials)\/\d+/);
        
        cy.contains('button', 'Retour').click();
        cy.wait(2000);
    });

    it('Tests des Filtres (Ralenti)', () => {
        // Filtre événements passés
        cy.get('input[type="checkbox"]').check({ force: true });
        cy.wait(3000); // On vérifie que la liste  a changé

        // Filtre Date
        cy.get('input[type="date"]').type('2025-01-01');
        cy.contains('100m Trial Final').should('not.exist');
        cy.wait(3000); // On observe le résultat pour cette date spécifique
        cy.contains('Marathon Qualification').should('exist');
    });

    it('Navigation Header (Ralenti)', () => {
        const pages = ['Championnats', 'Connexion', 'Inscription'];
        
        pages.forEach(page => {
            cy.contains('nav a', page).click();
            cy.wait(3000); 
            cy.get('.navbar-brand a').click(); // Retour au bercail
            cy.wait(1000);
        });
    });
});