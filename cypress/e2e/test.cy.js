describe('Page d’accueil', () => {
  it('doit afficher la page', () => {
    cy.visit('/');                  // ira sur http://localhost:3000
    cy.get('body').should('be.visible');
  });
});
