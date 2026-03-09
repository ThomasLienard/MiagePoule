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
        cy.get('input[type="email"]').type('anna@example.com');
        cy.wait(500);
        cy.get('input[type="password"]').type('test123');
        cy.wait(500);

        // Soumettre le formulaire
        cy.get('button[type="submit"]').click();
        cy.wait(3000);
    });

});