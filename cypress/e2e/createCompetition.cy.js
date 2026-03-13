describe('Tests Création de Compétition', () => {

    // Fonction pour injecter les dates malgré le blocage clavier
    const setDateValue = (selector, value) => {
        cy.get(selector).then(($el) => {
            const el = $el[0];
            const nativeInputValueSetter = Object.getOwnPropertyDescriptor(
                window.HTMLInputElement.prototype,
                'value'
            ).set;
            nativeInputValueSetter.call(el, value);
            el.dispatchEvent(new Event('input', { bubbles: true }));
            el.dispatchEvent(new Event('change', { bubbles: true }));
        });
    };

    beforeEach(() => {
        cy.on('uncaught:exception', () => false);

        // Connexion
        cy.visit('/login');
        cy.get('input[placeholder="Email"]').type('anna@example.com');
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.get('button[type="submit"]').click();

        cy.url().should('not.include', '/login');
        cy.visit('/admin/create-comp/');

        cy.intercept('GET', '**/public/championship').as('getChamps');
        cy.wait('@getChamps');
    });

    it('Cas 1 : Erreurs de champs vides (Validation HTML5)', () => {
        cy.get('button[type="submit"]').click();

        cy.get('form').should('have.class', 'was-validated');
        cy.contains('Le nom est requis').should('be.visible');
        cy.contains('La date de début est requise').should('be.visible');
    });

    it('Cas 2 : Erreur de chronologie (Date Fin < Date Début)', () => {
        cy.get('select[name="championshipId"]').select('1');
        cy.get('input[name="name"]').type('Test Chrono Inversé');
        cy.get('textarea[name="description"]').type('Début après la fin');

        setDateValue('input[name="start"]', '2025-01-02');
        setDateValue('input[name="end"]', '2025-01-01');

        cy.get('button[type="submit"]').click();

        cy.get('form').should('have.class', 'was-validated');

        cy.contains('La date de fin est requise').should('be.visible');
    });

    it('Cas 3 : Erreur hors limites (Dates hors championnat)', () => {
        cy.get('select[name="championshipId"]').select('1');
        cy.get('input[name="name"]').type('Test Hors Limites');
        cy.get('textarea[name="description"]').type('Dates en 2026');

        setDateValue('input[name="start"]', '2026-01-01');
        setDateValue('input[name="end"]', '2026-01-02');

        cy.get('button[type="submit"]').click();

        cy.get('form').should('have.class', 'was-validated');

        cy.contains('La date de début est requise').should('be.visible');
        cy.contains('La date de fin est requise').should('be.visible');
    });

    it('Cas 4 : Succès de création', () => {
        cy.get('select[name="championshipId"]').select('1');
        cy.get('input[name="name"]').type('Compétition Olympique');
        cy.get('textarea[name="description"]').type('Tout est en ordre');

        setDateValue('input[name="start"]', '2025-01-01');
        setDateValue('input[name="end"]', '2025-01-02');

        cy.intercept('POST', '**/admin/comps').as('saveRequest');
        cy.get('button[type="submit"]').click();

        cy.wait('@saveRequest').its('response.statusCode').should('eq', 201);

        cy.get('.alert-success').should('contain', 'Compétition planifiée avec succès');

        cy.url({ timeout: 5000 }).should('include', '/admin');
    });
});