describe('Page de Création d\'Évènement', () => {

    const setDateTimeValue = (selector, value) => {
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

        cy.visit('/login');
        cy.get('input[placeholder="Email"]').type('anna@example.com');
        cy.get('input[placeholder="Mot de passe"]').type('test123');
        cy.get('button[type="submit"]').click();
        cy.url().should('not.include', '/login');

        cy.intercept('GET', '**/public/championship', { fixture: 'championships.json' }).as('getChamps');
        cy.intercept('GET', '**/public/championship/*/comp', { fixture: 'competitions.json' }).as('getComps');
        cy.intercept('POST', '**/admin/events', { statusCode: 201, body: { message: 'Success' } }).as('postEvent');

        cy.visit('/admin/create-event/');
        cy.wait('@getChamps');
    });

    it('Cas 1 : Validation des champs vides', () => {
        cy.get('button[type="submit"]').click();
        cy.get('form').should('have.class', 'was-validated');
        cy.contains('Le nom de l\'évènement est requis').should('be.visible');
    });

    it('Cas 2 : Erreur si Fin < Début', () => {
        cy.get('select').first().select('1'); // World Cup
        cy.wait('@getComps');
        cy.get('select[name="competitionId"]').select('1'); // 100m Sprint

        cy.get('input[name="name"]').type('Finale 100m');
        cy.get('textarea[name="description"]').type('La grande finale');

        setDateTimeValue('input[name="startTime"]', '2025-01-01T14:00');
        setDateTimeValue('input[name="endTime"]', '2025-01-01T10:00');

        cy.get('input[name="placeName"]').type('Stade de France');
        cy.get('input[name="number"]').type('1');
        cy.get('input[name="street"]').type('Rue du test');
        cy.get('input[name="zipCode"]').type('93200');
        cy.get('input[name="city"]').type('Saint-Denis');

        cy.get('button[type="submit"]').click();

        cy.get('.alert-danger')
            .should('be.visible')
            .and('contain', 'La date de fin doit être strictement après la date de début');
    });

    it('Cas 3 : Création avec succès', () => {
        cy.get('select').first().select('1');
        cy.wait('@getComps');
        cy.get('select[name="competitionId"]').select('1');

        cy.get('input[name="name"]').type('Épreuve Valide');
        cy.get('select[name="typeEventName"]').select('TRIAL');
        cy.get('textarea[name="description"]').type('Description valide');

        setDateTimeValue('input[name="startTime"]', '2025-01-01T10:00');
        setDateTimeValue('input[name="endTime"]', '2025-01-01T12:00');

        cy.get('input[name="placeName"]').type('Stade de France');
        cy.get('input[name="number"]').type('11');
        cy.get('input[name="street"]').type('Avenue Jules Rimet');
        cy.get('input[name="zipCode"]').type('93200');
        cy.get('input[name="city"]').type('Saint-Denis');
        cy.get('#hasParking').check({ force: true });

        cy.get('button[type="submit"]').click();

        cy.wait('@postEvent').then((interception) => {
            expect(interception.request.body.name).to.equal('Épreuve Valide');
            expect(interception.request.body.hasParking).to.be.true;
        });

        cy.get('.alert-success').should('contain', 'Évènement planifié avec succès');
        cy.url({ timeout: 5000 }).should('include', '/admin');
    });

    it('Cas : Doit bloquer si les dates sont hors limites de la compétition', () => {
        cy.get('select').first().select('1');

        cy.wait('@getComps');

        cy.get('select[name="competitionId"]').select('1');

        cy.get('input[name="name"]').type('Event Test');
        cy.get('textarea[name="description"]').type('Description de test');
        cy.get('input[name="placeName"]').type('Stade');
        cy.get('input[name="number"]').type('1');
        cy.get('input[name="street"]').type('Rue');
        cy.get('input[name="zipCode"]').type('12345');
        cy.get('input[name="city"]').type('Ville');

        const startInvalide = '2026-01-01T10:00';
        const endInvalide = '2026-01-01T12:00';

        cy.get('input[name="startTime"]').type(startInvalide);
        cy.get('input[name="endTime"]').type(endInvalide);

        cy.get('button[type="submit"]').click();
        cy.get('.alert-danger', { timeout: 10000 })
            .should('be.visible')
            .and('contain', 'Les dates doivent être comprises entre');
    });
});