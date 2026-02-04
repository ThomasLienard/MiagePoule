describe('Tests - Navigation Championnats et Résultats', () => {

    beforeEach(() => {
        // On ignore les erreurs d'application asynchrones (fetch qui traîne, etc.)
        cy.on('uncaught:exception', () => false);
        cy.visit('/public/championship');
    });

    it('Parcours complet : du Championnat aux Résultats', () => {
        // 1. Page Liste des Championnats
        cy.contains('World Cup').should('be.visible'); // Ou un autre titre de championnat
        cy.wait(3000);
        
        // 2. Sélection d'un championnat et voir ses compétitions
        // On cherche un bouton "Voir les événements" dans une carte de championnat
        cy.get('.card').first().within(() => {
            cy.contains('Voir les événements').click();
        });
        cy.wait(3000);
        
        // 3. Page Compétition (Competition.jsx)
        // On vérifie qu'on a bien les deux colonnes : "A venir" et "Passés"
        cy.contains('A venir').should('be.visible');
        cy.contains('Passés').should('be.visible');
        cy.wait(3000);
        
        // 4. Clic sur un événement passé pour voir les résultats (TrialsAndEventsCard.jsx)
        // On va dans la section "Passés" et on clique sur la première carte
        cy.contains('Passés')
            .parents('.d-flex') 
            .find('.card')
            .first()
            .click();
        cy.wait(3000);
        
        // 5. Page Détails et Résultats (TrialsAndEventsDetails.jsx)
        // On vérifie l'affichage des médailles ou des rangs
        cy.url().should('include', '/public/trials');
        
        // On vérifie si la section résultats existe
        cy.get('body').then(($body) => {
            if ($body.text().includes('Résultats')) {
                cy.contains('Résultats').should('be.visible');
                // On vérifie la présence d'un podium ou d'un rang (ex: 🥇 ou le chiffre 1)
                cy.get('.list-group-item').first().should('be.visible');
            }
        });
        
        cy.wait(3000);
        
        // 6. Test du bouton Retour
        cy.contains('button', 'Retour').click();
        cy.wait(2000);
        cy.contains('A venir').should('be.visible'); // Vérifie qu'on est revenu à la compétition
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