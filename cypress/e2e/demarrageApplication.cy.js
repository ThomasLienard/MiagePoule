describe('Page d’accueil', () => {
  it('doit afficher la page', () => {
    cy.visit('/');
    cy.get('body').should('be.visible');
  });})