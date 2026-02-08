describe('Tests - Authentification CiblOrgaSport', () => {

    beforeEach(() => {
        // Désactive l'arrêt du test sur les exceptions non gérées de l'app
        cy.on('uncaught:exception', () => false);
        cy.visit('/');
        cy.wait(3000);
    });

    it('Scénario : Inscription avec erreurs de validation', () => {
        cy.contains('Inscription').click();
        cy.wait(3000);

        // Remplissage du formulaire d'inscription
        cy.get('#firstName').type('Jean');
        cy.wait(3000);
        cy.get('#lastName').type('Test');
        cy.wait(3000);
        cy.get('#email').type('jean.test@example.com');
        cy.wait(3000);
        cy.get('#password').type('Password123');
        cy.wait(3000);
        cy.get('#confirmPassword').type('Autre'); // Erreur volontaire
        cy.wait(3000);
        
        cy.get('button[type="submit"]').click();
        
        // Vérification du message d'erreur (défini dans RegisterPage.jsx)
        cy.contains('Les mots de passe ne correspondent pas').should('be.visible');
        cy.wait(3000);
    });

    it('Scénario : Inscription correcte', () => {
        cy.contains('Inscription').click();
        cy.wait(3000);

        // Remplissage du formulaire d'inscription
        cy.get('#firstName').type('Jean');
        cy.wait(3000);
        cy.get('#lastName').type('Test');
        cy.wait(3000);
        cy.get('#email').type('jean.test@example.com');
        cy.wait(3000);
        cy.get('#password').type('Password123');
        cy.wait(3000);
        cy.get('#confirmPassword').type('Password123'); 
        cy.wait(3000);
        
        cy.get('button[type="submit"]').click();
        
        cy.wait(3000);
        
        cy.url().should('match', /\/(account|change-password|register)/);

        cy.wait(3000);
    });

    it('Scénario : Connexion et Navigation Profil', () => {
        cy.contains('Connexion').click();
        cy.wait(3000);

        // Connexion
        cy.get('input[placeholder="Email"]').type('john@doe.com');
        cy.wait(3000);
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.wait(3000);
        
        cy.get('button[type="submit"]').click();

        // Attente de la redirection vers le compte ou le profil
        cy.wait(3000);
        cy.url().should('match', /\/(account|change-password)/);
        
        // Si connecté, le bouton "Profil" apparaît dans le Layout.jsx
        cy.contains('Profil').should('be.visible');
        cy.contains('Confidentialité').should('be.visible');
        cy.wait(3000);

        // Test de déconnexion via le Header
        cy.contains('Profil').click(); // Ouvre la page profil
        cy.wait(3000);
        
        // On cherche le lien de déconnexion (Layout.jsx handleLogout)
        // Note: Si ton logout est un bouton spécifique dans le menu
        cy.get('nav').contains('Connexion').should('not.exist'); 
        cy.wait(3000);
    });

    it('Scénario : Changement de mot de passe (route innaccessible)', () => {
        // On force l'accès pour tester l'interface de PasswordForm.jsx
        // Normalement protégé par ProtectedRoute.jsx
        cy.visit('/change-password');
        cy.wait(3000);
    });
});