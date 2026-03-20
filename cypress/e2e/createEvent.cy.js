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

        cy.intercept('GET', '**/public/championship', { fixture: 'championships.json' }).as('getChamps');
        cy.intercept('GET', '**/public/championship/*/comp', { fixture: 'competitions.json' }).as('getComps');
        cy.intercept('GET', '**/commissaire/users?role=COMMISSAIRE', {
            body: [{ id: 50, firstName: 'Jean', lastName: 'Dupont', email: 'jean@test.com' }]
        }).as('getComm');
        cy.intercept('POST', '**/admin/events', { statusCode: 201, body: { message: 'Success' } }).as('postEvent');

        cy.visit('/login');
        cy.get('input[type="email"]').type('anna@example.com');
        cy.get('input[type="password"]').type('test123');
        cy.get('button[type="submit"]').click();

        cy.url().should('not.include', '/login');

        cy.visit('/admin/create-event/');

        cy.wait('@getChamps', { timeout: 10000 });
    });

    it('Cas 1 : Validation des champs vides', () => {
        cy.get('button[type="submit"]').click();
        cy.get('form').should('have.class', 'was-validated');
    });

    it('Cas 2 : Erreur si Fin < Début', () => {
        cy.get('select').first().select('1');
        cy.wait('@getComps');

        cy.get('select[name="competitionId"]').select('1');
        cy.get('input[name="name"]').type('Nom Test');
        cy.get('textarea[name="description"]').type('Description Test');

        cy.get('input[name="placeName"]').type('Stade');
        cy.get('input[name="number"]').type('1');
        cy.get('input[name="street"]').type('Rue du Test');
        cy.get('input[name="zipCode"]').type('75000');
        cy.get('input[name="city"]').type('Paris');

        cy.get('input[name="startTime"]').should('not.be.disabled');

        setDateTimeValue('input[name="startTime"]', '2025-05-10T14:00');
        setDateTimeValue('input[name="endTime"]', '2025-05-10T10:00');

        cy.get('button[type="submit"]').click();

        cy.get('.alert-danger', { timeout: 10000 })
            .should('exist')
            .and('be.visible')
            .and('contain', 'Veuillez remplir tous les champs obligatoires correctement.');
    });

    it('Cas 3 : Création avec succès (Trial + Commissaire)', () => {
        cy.get('select').first().select('1');
        cy.wait('@getComps');
        cy.get('select[name="competitionId"]').select('1');

        cy.get('input[name="name"]').type('Épreuve Valide');
        cy.get('select[name="typeEventName"]').select('TRIAL');

        cy.get('select[name="commissaireId"]').should('be.visible').select('50');

        cy.get('textarea[name="description"]').type('Description valide');

        setDateTimeValue('input[name="startTime"]', '2025-01-01T10:00');
        setDateTimeValue('input[name="endTime"]', '2025-01-01T12:00');

        cy.get('input[name="placeName"]').type('Stade de France');
        cy.get('input[name="number"]').type('11');
        cy.get('input[name="street"]').type('Avenue Jules Rimet');
        cy.get('input[name="zipCode"]').type('93200');
        cy.get('input[name="city"]').type('Saint-Denis');

        cy.get('input[name="hasParking"]').check({ force: true });

        cy.get('button[type="submit"]').click();

        cy.wait('@postEvent').then((interception) => {
            expect(interception.request.body.name).to.equal('Épreuve Valide');
            expect(interception.request.body.commissaireId).to.equal(50);
        });

        cy.get('.alert-success').should('contain', 'Évènement planifié avec succès');
    });

    it('Cas 4 : Le champ commissaire ne doit apparaître QUE pour un TRIAL', () => {
        cy.get('select[name="commissaireId"]').should('not.exist');

        cy.get('select[name="typeEventName"]').select('TRIAL');
        cy.get('select[name="commissaireId"]').should('be.visible');

        cy.get('select[name="typeEventName"]').select('TRAINING');
        cy.get('select[name="commissaireId"]').should('not.exist');
    });

    it('Cas 5 : Le commissaire doit être vidé si on change le type après sélection', () => {
        cy.get('select').first().select('1');
        cy.wait('@getComps');
        cy.get('select[name="competitionId"]').select('1');

        cy.get('select[name="typeEventName"]').select('TRIAL');
        cy.wait('@getComm');
        cy.get('select[name="commissaireId"]').select('50');

        cy.get('select[name="typeEventName"]').select('MEETING');

        cy.get('input[name="name"]').type('Test Reset');
        cy.get('textarea[name="description"]').type('Desc');
        cy.get('input[name="placeName"]').type('Lieu');
        cy.get('input[name="number"]').type('1');
        cy.get('input[name="street"]').type('Rue');
        cy.get('input[name="zipCode"]').type('12345');
        cy.get('input[name="city"]').type('Ville');
        setDateTimeValue('input[name="startTime"]', '2025-01-01T10:00');
        setDateTimeValue('input[name="endTime"]', '2025-01-01T11:00');

        cy.get('button[type="submit"]').click();

        cy.wait('@postEvent').then((interception) => {
            expect(interception.request.body.typeEventName).to.equal('MEETING');
            expect(interception.request.body.commissaireId).to.satisfy((val) => val === null || val === '');
        });
    });
});