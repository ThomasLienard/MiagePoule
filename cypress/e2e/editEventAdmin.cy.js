describe("Tests - Modification d'épreuve (Admin)", () => {

    const eventsFixturePath = 'editEventAdmin/allEvents.json'
    const championshipFixturePath = 'editEventAdmin/allChampionships.json'
    const eventDetailFixturePath = 'editEventAdmin/eventDetail.json'
    const competitionsFixturePath = 'editEventAdmin/allCompetitions.json'

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

        cy.intercept('GET', '**/public/championship', { fixture: championshipFixturePath }).as('getChampionships');
        cy.intercept('GET', '**/public/events', { fixture: eventsFixturePath }).as('getEvents');

        cy.intercept('GET', '**/commissaire/users?role=COMMISSAIRE', {
            body: [
                { id: 50, firstName: 'Jean', lastName: 'Dupont', email: 'jean@test.com' },
                { id: 51, firstName: 'Marc', lastName: 'Durand', email: 'marc@test.com' }
            ]
        }).as('getComms');

        cy.visit('/login');
        cy.get('input[type="email"]').type('anna@example.com');
        cy.get('input[type="password"]').type('test123');
        cy.get('button[type="submit"]').click();

        cy.url().should('not.include', '/login');
        cy.visit('/admin/update-event');

        cy.wait(['@getChampionships', '@getEvents', '@getComms']);
    });

    it('Scénario : Modification d\'une épreuve (Succès standard)', () => {
        cy.intercept('GET', '**/public/events/1', { fixture: eventDetailFixturePath }).as('getEventDetail');
        cy.intercept('GET', '**/public/championship/*/comp', { fixture: competitionsFixturePath }).as('getAllCompetitions');
        cy.intercept('PUT', '**/admin/events/1', { statusCode: 200 }).as('updateEvent');

        cy.get("select").first().select('1');

        cy.wait("@getEventDetail");
        cy.wait("@getAllCompetitions");

        cy.get("input[name='name']").clear().type("100m Trial Heat 2");

        setDateTimeValue("input[name='startTime']", "2025-03-21T10:00");
        setDateTimeValue("input[name='endTime']", "2025-03-21T11:00");

        cy.contains("Enregistrer les modifications").click();

        cy.wait("@updateEvent").then((interception) => {
            expect(interception.request.body.name).to.equal("100m Trial Heat 2");
        });

        cy.url().should('include', '/admin');
    });

    it('Scénario : Modification d\'une épreuve - Echec (Validation)', () => {
        cy.intercept('GET', '**/public/events/1', { fixture: eventDetailFixturePath }).as('getEventDetail');
        cy.intercept('GET', '**/public/championship/*/comp', { fixture: competitionsFixturePath }).as('getAllCompetitions');

        cy.get("select").first().select('1');
        cy.wait(["@getEventDetail", "@getAllCompetitions"]);

        cy.get("input[name='name']").clear();

        cy.intercept('PUT', '**/admin/events/1', {
            statusCode: 400,
            body: { message: "Erreur lors de la sauvegarde." }
        }).as('updateError');

        cy.contains("Enregistrer les modifications").click();

        cy.wait('@updateError');
        cy.get('.alert-danger').should('be.visible').and('contain', "Erreur lors de la sauvegarde.");
    });

    it('Scénario : Modification du commissaire (uniquement pour un TRIAL)', () => {
        cy.intercept('GET', '**/public/events/1', { fixture: eventDetailFixturePath }).as('getEventDetail');
        cy.intercept('GET', '**/public/championship/*/comp', { fixture: competitionsFixturePath }).as('getAllCompetitions');
        cy.intercept('PUT', '**/admin/events/1', { statusCode: 200 }).as('updateEvent');

        cy.get("select").first().select('1');
        cy.wait(["@getEventDetail", "@getAllCompetitions"]);

        cy.get("select[name='typeEventName']").select('TRIAL');

        cy.get("select[name='commissaireId']")
            .should('be.visible')
            .select('50');

        cy.contains("Enregistrer les modifications").click();

        cy.wait("@updateEvent").then((interception) => {
            expect(interception.request.body.commissaireId).to.equal(50);
            expect(interception.request.body.typeEventName).to.equal('TRIAL');
        });
    });

    it('Vérification métier : Le commissaire est null si le type n\'est pas TRIAL', () => {
        cy.intercept('GET', '**/public/events/1', { fixture: eventDetailFixturePath }).as('getEventDetail');
        cy.intercept('GET', '**/public/championship/*/comp', { fixture: competitionsFixturePath }).as('getAllCompetitions');
        cy.intercept('PUT', '**/admin/events/1', { statusCode: 200 }).as('updateEvent');

        cy.get("select").first().select('1');
        cy.wait(["@getEventDetail", "@getAllCompetitions"]);

        cy.get("select[name='typeEventName']").select('MEETING');

        cy.get("select[name='commissaireId']").should('not.exist');

        cy.contains("Enregistrer les modifications").click();

        cy.wait("@updateEvent").then((interception) => {
            expect(interception.request.body.commissaireId).to.be.null;
            expect(interception.request.body.typeEventName).to.equal('MEETING');
        });
    });

    it('Scénario : Affichage conditionnel du bloc commissaire', () => {
        cy.get("select").first().select('1');

        // Par défaut, si MEETING, le champ n'existe pas
        cy.get("select[name='typeEventName']").select('MEETING');
        cy.get("select[name='commissaireId']").should('not.exist');

        // En TRIAL, il apparaît
        cy.get("select[name='typeEventName']").select('TRIAL');
        cy.get("select[name='commissaireId']").should('be.visible');

        // En TRAINING, il disparaît à nouveau
        cy.get("select[name='typeEventName']").select('TRAINING');
        cy.get("select[name='commissaireId']").should('not.exist');
    });

});