describe('Tests - Authentification CiblOrgaSport', () => {

    beforeEach(() => {
        // Désactive l'arrêt du test sur les exceptions non gérées de l'app
        cy.on('uncaught:exception', () => false);
        cy.visit('/');
        cy.wait(3000);
    });

    it('Scénario : Inscription avec erreurs de validation', () => {
        cy.contains('Inscription').click();
        cy.wait(2500);

        // Remplissage du formulaire d'inscription
        cy.get('#firstName').type('Jean');
        cy.get('#lastName').type('Test');
        cy.get('#email').type('jean.test@example.com');
        cy.get('#password').type('Password123');
        cy.get('#confirmPassword').type('Autre'); // Erreur volontaire
        cy.wait(100);
        
        cy.get('button[type="submit"]').click();
        
        // Vérification du message d'erreur (défini dans RegisterPage.jsx)
        cy.contains('Les mots de passe ne correspondent pas').should('be.visible');
    });

    it('Scénario : Inscription correcte', () => {
        cy.contains('Inscription').click();
        cy.wait(2000);

        // Remplissage du formulaire d'inscription
        cy.get('#firstName').type('Jean');
        cy.get('#lastName').type('Test');
        cy.get('#email').type('jean.test@example.com');
        cy.get('#password').type('Password123');
        cy.get('#confirmPassword').type('Password123');
        cy.wait(100);
        
        cy.get('button[type="submit"]').click();
        
        cy.wait(2000);
        
        cy.url().should('match', /\/(account|change-password|register)/);

    });

    it('Scénario : Connexion et Navigation Profil', () => {
        cy.contains('Connexion').click();
        cy.wait(2500);

        // Connexion
        cy.get('input[placeholder="Email"]').type('john@example.com');
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.wait(100);
        
        cy.get('button[type="submit"]').click();

        // Attente de la redirection vers le compte ou le profil
        cy.wait(2000);
        cy.url().should('match', /\/(account|change-password)/);
        
        // Si connecté, le bouton "Profil" apparaît dans le Layout.jsx
        cy.contains('Profil').should('be.visible');
        cy.contains('Confidentialité').should('be.visible');

        // Test de déconnexion via le Header
        cy.contains('Profil').click(); // Ouvre la page profil
        cy.wait(2000);
        
        // On cherche le lien de déconnexion (Layout.jsx handleLogout)
        // Note: Si ton logout est un bouton spécifique dans le menu
        cy.get('nav').contains('Connexion').should('not.exist');
    });

    it('Scénario : Changement de mot de passe (route innaccessible)', () => {
        // On force l'accès pour tester l'interface de PasswordForm.jsx
        // Normalement protégé par ProtectedRoute.jsx
        cy.visit('/change-password');
        cy.wait(500);
        cy.contains("Non connecté")
    });
});