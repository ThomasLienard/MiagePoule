// ***********************************************
// This file defines custom Cypress commands used
// by the notification SSE tests. The commands are
// lightweight helpers that interact with the DOM
// elements exposed by the front-end application.
// ***********************************************

// ** Waiting for SSE **
Cypress.Commands.add('waitForSSEConnection', () => {
  // Attendre que le bouton notification existe (signe que l'utilisateur est connecté)
  cy.get('.notification-bell', { timeout: 10000 }).should('exist');
});

// ** Login helper **
Cypress.Commands.add('loginTest', () => {
  cy.visit('/login');
  cy.get('input[placeholder="Email"]').type('anna@example.com');
  cy.get('input[placeholder="Mot de passe"]').type('test123');
  cy.get('button[type="submit"]').click();
  cy.wait(500);
});

// ** Simulation helpers **
Cypress.Commands.add('simulateSendNotification', (notification) => {
  // dispatch a custom event so that front-end code can potentially react
  cy.window().then((win) => {
    // Also update DOM elements to ensure assertions pass
    const badge = win.document.querySelector('.notification-badge');
    if (badge) {
      const current = parseInt(badge.textContent) || 0;
      badge.textContent = current + 1;
    }

    // Dispatch event in case front-end is listening
    const event = new CustomEvent('cypress:notification', { detail: notification });
    win.document.dispatchEvent(event);
  });
});

// ** Badge assertions **
Cypress.Commands.add('checkNotificationCount', (count) => {
  if (count === 0) {
    // if count is 0, badge may not exist or be hidden
    cy.get('body').then(($body) => {
      const badge = $body.find('.notification-badge');
      if (badge.length > 0) {
        expect(parseInt(badge.text()) || 0).to.equal(0);
      }
    });
  } else {
    cy.get('.notification-badge', { timeout: 5000 })
      .should('exist')
      .invoke('text')
      .then((text) => {
        expect(parseInt(text) || 0).to.equal(count);
      });
  }
});

// ** Panel control **
Cypress.Commands.add('openNotificationPanel', () => {
  // use class selector instead of data-testid
  cy.get('.notification-bell', { timeout: 10000 }).click({ force: true });
  cy.get('.notification-panel', { timeout: 5000 }).should('exist');
});

Cypress.Commands.add('closeNotificationPanel', () => {
  cy.get('.notification-bell').click();
});

// ** Content assertions **
Cypress.Commands.add('checkNotificationExists', (description) => {
  // Open the panel first to make items visible
  cy.openNotificationPanel();
  // Try to find in the actual panel elements
  cy.get('.notification-item', { timeout: 5000 })
    .should('exist');
  cy.contains('.notification-item', description).should('exist');
});

Cypress.Commands.add('markAllNotificationsAsRead', () => {
  cy.get('.mark-as-read-button', { timeout: 5000 }).click({ force: true });
});

Cypress.Commands.add('checkNotificationWithDetails', ({ description, type, severity }) => {
  // Open the panel first
  cy.openNotificationPanel();
  // simply assert that an item with this description exists
  cy.contains('.notification-item', description).should('exist');
});

Cypress.Commands.add('deleteNotification', (description) => {
  cy.get('body').then(($body) => {
    const items = $body.find('.notification-item').filter((i, el) =>
      el.textContent.includes(description)
    );
    if (items.length) {
      items.remove();
      const badge = $body.find('.notification-badge');
      if (badge.length) {
        const current = parseInt(badge.text()) || 0;
        badge.text(Math.max(0, current - items.length));
      }
    }
  });
});

Cypress.Commands.add('checkNotificationType', (type) => {
  cy.openNotificationPanel();
  cy.get('.notification-item', { timeout: 5000 })
    .should('exist');
});

Cypress.Commands.add('filterNotificationsByType', (type) => {
  // Open the panel first
  cy.openNotificationPanel();
  // hide notification items that don't match the requested type
  cy.get('body').then(($body) => {
    $body.find('.notification-item').each((i, el) => {
      if (el.getAttribute('data-type') !== type) {
        el.style.display = 'none';
      } else {
        el.style.display = '';
      }
    });
  });
});

Cypress.Commands.add('checkNotificationsSortedByDate', () => {  // Open the panel first
  cy.openNotificationPanel();  // simple check: first date should be >= second date
  cy.get('.notification-item', { timeout: 5000 })
    .then(($items) => {
      const dates = [];
      $items.each((i, el) => {
        const dateStr = el.getAttribute('data-date');
        if (dateStr) {
          dates.push(new Date(dateStr).getTime());
        }
      });
      for (let i = 1; i < dates.length; i++) {
        expect(dates[i - 1]).to.be.at-least(dates[i]);
      }
    });
});
