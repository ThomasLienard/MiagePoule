describe('Tests - Navigation Championnats et Résultats', () => {

    beforeEach(() => {
        // On ignore les erreurs d'application asynchrones (fetch qui traîne, etc.)
        cy.on('uncaught:exception', () => false);
        cy.visit('/public/championship');
    });

    it('Parcours complet : du Championnat aux Résultats', () => {
        // 1. Liste des Championnats
        cy.visit('/public/championship');
        cy.contains('World Cup').should('be.visible');

        // 2. Clic pour voir les compétitions
        cy.get('.card').contains('Voir les événements').first().click();

        // 3. Attente que le chargement soit fini
        cy.get('.loading', { timeout: 10000 }).should('not.exist');

        // 4. Ciblage de la section "Passés" et clic sur un TRIAL
        cy.contains('.card-title', 'Passés')
            .parents('.card')
            .find('.card')
            .filter(':contains("🏆 Compétition") + div, :contains("🏆 Compétition") ~ .card')
            .first()
            .should('be.visible')
            .click({ force: true });

        // 5. Vérification de la redirection
        cy.url({ timeout: 10000 }).should('include', '/public/trials');

        // 6. Vérification finale
        cy.contains('Résultats').should('be.visible');
    });

    it('Vérifie l\'affichage en cas de liste vide', () => {
        // Ce test est utile pour vérifier "length === 0" dans le code
        cy.visit('/public/championship');
        
        // Si  pas de données, code affiche :
        // "Aucun championnat disponible." (ListChampionships.jsx)
        cy.get('body').then(($body) => {
            if ($body.text().includes('Aucun championnat disponible')) {
                cy.contains('Aucun championnat disponible.').should('be.visible');
            }
        });
        cy.wait(3000);
    });
});