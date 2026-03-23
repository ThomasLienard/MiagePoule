import {format} from "date-fns";
import {fr} from "date-fns/locale";

const formatFileSize = (bytes) => {
    if (!bytes) return '-';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
};

describe("Tests - Gestion des billets", () => {

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
        cy.get('input[type="email"]').type('athlete@example.com');
        cy.wait(500);
        cy.get('input[type="password"]').type('test123');
        cy.wait(500);

        // Soumettre le formulaire
        cy.get('button[type="submit"]').click();
        cy.wait(2000);
    });

    it('display every tickets', () => {
        const fixturePath = "tickets/allTickets.json"

        cy.intercept('GET', `**/documents/tickets`,
            { fixture: fixturePath}
        ).as('allTickets');

        cy.contains('📄 Mes Billets').click();
        cy.wait('@allTickets');

        cy.wait(200);

        cy.fixture(fixturePath).then((tickets) => {
            for (const ticket of tickets) {
                cy.contains(ticket.originalFileName).should('be.visible')
                    .parentsUntil(".table").as('ticketTable');

                if (!ticket.description) {
                    cy.get('@ticketTable').contains("-").should('be.visible');
                } else {
                    cy.get('@ticketTable').contains(ticket.description).should('be.visible');
                }

                const formatedDate = format(new Date(ticket.uploadedAt), 'dd/MM/yyyy HH:mm', { locale: fr })
                cy.get('@ticketTable').contains(formatedDate).should('be.visible');

                const formatedFileSize = formatFileSize(ticket.fileSize);
                cy.get('@ticketTable').contains(formatedFileSize).should('be.visible');
            }
        })
    });

    it('should add tickets', () => {
        const fixturePath = "tickets/allTickets.json"
        const fixturePathNewTickets = "tickets/allTicketsWithNew.json"

        cy.intercept('GET', `**/documents/tickets`,
            { fixture: fixturePath}
        ).as('allTickets');

        cy.contains('📄 Mes Billets').click();
        cy.wait('@allTickets');

        cy.wait(200);

        cy.contains('Ajouter un billet').click();

        cy.get('.modal').within(() => {
            cy.get('input[type="file"]').selectFile('cypress/fixtures/tickets/Super ticket 64.pdf')
            cy.get('textarea').type('Aude aux amis');

            cy.intercept('POST','**/documents/tickets/upload',
                {statusCode: 201}
            ).as('addTicket')

            cy.intercept('GET', `**/documents/tickets`,
                { fixture: fixturePathNewTickets}
            ).as('getAllTicketsWithNew');

            // Soumettre le formulaire
            cy.contains('button', 'Uploader').click();
        });

        cy.wait('@addTicket')
        cy.wait('@getAllTicketsWithNew')

        cy.wait(500);

        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');

        cy.fixture(fixturePathNewTickets).then((tickets) => {
            for (const ticket of tickets) {
                cy.contains(ticket.originalFileName).should('be.visible')
                    .parentsUntil(".table").as('ticketTable');

                if (!ticket.description) {
                    cy.get('@ticketTable').contains("-").should('be.visible');
                } else {
                    cy.get('@ticketTable').contains(ticket.description).should('be.visible');
                }

                const formatedDate = format(new Date(ticket.uploadedAt), 'dd/MM/yyyy HH:mm', { locale: fr })
                cy.get('@ticketTable').contains(formatedDate).should('be.visible');

                const formatedFileSize = formatFileSize(ticket.fileSize);
                cy.get('@ticketTable').contains(formatedFileSize).should('be.visible');
            }
        })
    });

    it('should remove tickets', () => {
        const fixturePath = "tickets/allTicketsWithNew.json"
        const fixturePathWithRemovedTicket = "tickets/allTickets.json"

        cy.intercept('GET', `**/documents/tickets`,
            { fixture: fixturePath}
        ).as('allTickets');

        cy.contains('📄 Mes Billets').click();
        cy.wait('@allTickets');

        cy.wait(200);

        cy.contains('tr', 'Super ticket 64.pdf')
            .find('button[title="Supprimer"]')
            .click();


        cy.get('.modal').within(() => {
            cy.intercept('DELETE','**/documents/tickets/3',
                {statusCode: 204}
            ).as('removeTicket')

            // Soumettre le formulaire
            cy.contains('button', 'Supprimer').click();
        });

        cy.wait('@removeTicket');
        cy.wait(500);

        // Vérifier que la modal se ferme
        cy.get('.modal').should('not.exist');

        cy.fixture(fixturePathWithRemovedTicket).then((tickets) => {
            for (const ticket of tickets) {
                cy.contains(ticket.originalFileName).should('be.visible')
                    .parentsUntil(".table").as('ticketTable');

                if (!ticket.description) {
                    cy.get('@ticketTable').contains("-").should('be.visible');
                } else {
                    cy.get('@ticketTable').contains(ticket.description).should('be.visible');
                }

                const formatedDate = format(new Date(ticket.uploadedAt), 'dd/MM/yyyy HH:mm', { locale: fr })
                cy.get('@ticketTable').contains(formatedDate).should('be.visible');

                const formatedFileSize = formatFileSize(ticket.fileSize);
                cy.get('@ticketTable').contains(formatedFileSize).should('be.visible');
            }
        })
    });

});