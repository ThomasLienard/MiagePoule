// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })

// ---- Notification helpers ------------------------------------------------

// wait until the application's SSE connection is established
Cypress.Commands.add('waitForSSEConnection', () => {
    cy.window().should((win) => {
        // eventSourceRef is created by the hook in the application
        expect(win.eventSourceRef, 'sse reference').to.exist;
        expect(win.eventSourceRef.current, 'event source object').to.exist;
        expect(win.eventSourceRef.current.readyState, 'readyState').to.equal(1);
    });
});

// open / close notification panel by clicking on the toggle control
Cypress.Commands.add('openNotificationPanel', () => {
    cy.get('body').then(($body) => {
        if ($body.find('[data-testid="notification-button"]').length > 0) {
            cy.get('[data-testid="notification-button"]').click();
        } else if ($body.find('.notification-bell').length > 0) {
            cy.get('.notification-bell').click();
        } else if ($body.find('[data-testid="notification-badge"]').length > 0) {
            // some implementations open panel when clicking badge
            cy.get('[data-testid="notification-badge"]').click();
        }
    });
});

Cypress.Commands.add('closeNotificationPanel', () => {
    cy.get('body').then(($body) => {
        // try toggling same button or clicking outside
        if ($body.find('[data-testid="notification-button"]').length > 0) {
            cy.get('[data-testid="notification-button"]').click();
        } else if ($body.find('.notification-close').length > 0) {
            cy.get('.notification-close').click();
        } else {
            // click the overlay if present
            cy.get('body').click(0, 0);
        }
    });
});

// helper to dispatch a fake SSE message on the EventSource object
Cypress.Commands.add('simulateSendNotification', (notification) => {
    cy.window().then((win) => {
        const evt = new MessageEvent('message', {
            data: JSON.stringify(notification)
        });
        if (win.eventSourceRef && win.eventSourceRef.current) {
            win.eventSourceRef.current.dispatchEvent(evt);
        } else {
            // if no eventSourceRef, throw so the test fails loudly
            throw new Error('No EventSource available to send notification');
        }
    });
});

// assert the badge shows a given count
Cypress.Commands.add('checkNotificationCount', (count) => {
    cy.get('body').then(($body) => {
        const expected = count.toString();
        if ($body.find('[data-testid="notification-badge"]').length > 0) {
            cy.get('[data-testid="notification-badge"]').should('contain', expected);
        } else if ($body.find('.notification-count').length > 0) {
            cy.get('.notification-count').should('contain', expected);
        }
    });
});

// open panel and look for an item containing text
Cypress.Commands.add('checkNotificationExists', (text) => {
    cy.openNotificationPanel();
    cy.get('[data-testid="notification-item"]').contains(text).should('exist');
});

// click the "mark all as read" button if it exists
Cypress.Commands.add('markAllNotificationsAsRead', () => {
    cy.openNotificationPanel();
    cy.get('body').then(($body) => {
        if ($body.find('[data-testid="mark-as-read-button"]').length > 0) {
            cy.get('[data-testid="mark-as-read-button"]').click();
        } else if ($body.find('.mark-as-read').length > 0) {
            cy.get('.mark-as-read').click();
        }
    });
});

// check that a notification with certain properties is present
Cypress.Commands.add('checkNotificationWithDetails', (details) => {
    cy.openNotificationPanel();
    cy.get('[data-testid="notification-item"]').contains(details.description).parent().within(() => {
        if (details.type) {
            cy.contains(details.type);
        }
        if (details.severity) {
            cy.contains(details.severity);
        }
    });
});

// attempt to remove a notification by description
Cypress.Commands.add('deleteNotification', (description) => {
    cy.openNotificationPanel();
    cy.get('[data-testid="notification-item"]').contains(description).parent().then(($el) => {
        // try click a delete control
        if ($el.find('[data-testid="delete-button"]').length) {
            cy.wrap($el).find('[data-testid="delete-button"]').click({ force: true });
        } else if ($el.find('.delete-notification').length) {
            cy.wrap($el).find('.delete-notification').click({ force: true });
        } else {
            // fallback to swipe left
            cy.wrap($el).trigger('swipeleft');
        }
    });
});

// verify there is at least one notification with a given type string
Cypress.Commands.add('checkNotificationType', (type) => {
    cy.openNotificationPanel();
    cy.get('[data-testid="notification-item"]').contains(type).should('exist');
});

// open panel and filter using the UI
Cypress.Commands.add('filterNotificationsByType', (type) => {
    cy.openNotificationPanel();
    cy.get('body').then(($body) => {
        if ($body.find('[data-testid="notification-filter"]').length > 0) {
            cy.get('[data-testid="notification-filter"]').click();
            cy.get('[data-testid="filter-option"]').contains(type).click();
        }
    });
});

// ensure notifications in panel appear in descending date order if a date is present
Cypress.Commands.add('checkNotificationsSortedByDate', () => {
    cy.openNotificationPanel();
    cy.get('[data-testid="notification-item"]').then(($items) => {
        let prev = null;
        $items.each((index, el) => {
            const text = el.innerText;
            // attempt to parse ISO date inside the text
            const isoMatch = text.match(/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z/);
            if (isoMatch) {
                const date = new Date(isoMatch[0]);
                if (prev && date > prev) {
                    throw new Error('Notifications not sorted by date');
                }
                prev = date;
            }
        });
    });
});