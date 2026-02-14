-- ======================
-- Countries
-- ======================
INSERT INTO country (country_code) VALUES ('FR');
INSERT INTO country (country_code) VALUES ('US');
INSERT INTO country (country_code) VALUES ('DE');
INSERT INTO country (country_code) VALUES ('ES');
INSERT INTO country (country_code) VALUES ('IT');

-- ======================
-- Roles
-- ======================
INSERT INTO role (role_name) VALUES ('ADMIN');
INSERT INTO role (role_name) VALUES ('COMMISSAIRE');
INSERT INTO role (role_name) VALUES ('ATHLETE');
INSERT INTO role (role_name) VALUES ('VOLONTAIRE');
INSERT INTO role (role_name) VALUES ('SPECTATEUR');
-- ======================
-- Severities
-- ======================
INSERT INTO severity (name_severity, desc_severity)
VALUES ('INFO', 'Information message'),
       ('WARNING', 'Warning level'),
       ('CRITICAL', 'Critical event');

-- ======================
-- Type of documents
-- ======================
INSERT INTO type_of_document ( name_type_doc)
VALUES ( 'PASSPORT'),
       ( 'LICENSE');

-- ======================
-- Type of notification
-- ======================
INSERT INTO type_of_notification (name_type_of_notification)
VALUES ('EMAIL'),
       ('SMS'),
       ('SYSTEM');

-- ======================
-- Type events
-- ======================
INSERT INTO type_event (type_event_name)
VALUES ('MEETING'),
       ('TRAINING'),
       ('TRIAL');

-- ======================
-- Championships
-- ======================
INSERT INTO championship ( description_championship, name_championship,
                          start_date_championship, end_date_championship)
VALUES ( 'World level championship', 'World Cup', '2025-01-01', '2025-01-02'),
       ('National level championship', 'National League', '2025-01-01', '2025-01-02');

-- ======================
-- Competitions
-- ======================
INSERT INTO competition ( name_competition, description_competition,
                         id_championship, start_date_competition, end_date_competition)
VALUES ( '100m Sprint', 'Short distance run', 1, '2025-01-01', '2025-01-02'),
       ( 'Marathon', 'Long distance run', 1, '2025-01-01', '2025-01-02');

-- ======================
-- Places
-- ======================
INSERT INTO place (name_place, city_place, zip_code_place, street_place,
                   parking_place, number_place, description_place,
                   latitude_place, longitude_place)
VALUES ('Olympic Stadium', 'Paris', '75000', 'Main Street', TRUE, '10',
        'Central stadium', 48.85, 2.35);

-- ======================
-- Time slots
-- ======================
INSERT INTO time_slot (start_time, end_time)
VALUES ('2025-01-01 09:00:00', '2025-01-01 10:00:00'),
       ('2025-01-01 10:00:00', '2025-01-01 11:00:00');
-- ======================
-- Events
-- ======================
INSERT INTO event ( name_event, description_event, type_event_name,
                   id_place, id_time_slot, id_competition)
VALUES ( 'Morning Sprint Session', 'Speed training', 'TRAINING', 1, 1, 1),
       ( 'Final Sprint Race', 'Official competition', 'TRIAL', 1, 2, 2),
       ( '400m Trial', 'Medium distance race', 'TRIAL', 1, 2, 2),
       ('Team Relay Trial', 'Team competition', 'TRIAL', 1, 2, 2),
       ( 'Individual 800m Trial', 'Distance race', 'TRIAL', 1, 2, 2);

-- ======================
-- Trials
-- ======================
-- With JOINED inheritance, Trial shares the same primary key with Event
-- Event 1 becomes a Trial
-- Event 2 becomes a Trial  
-- Event 3 becomes a Trial
-- Event 4 becomes a Trial
-- Event 5 becomes a Trial
INSERT INTO trial (id_event)
VALUES (1),
       (2),
       (3),
       (4),
       (5);

-- ======================
-- Users (MODIFIÉ avec BCrypt)
-- ======================
-- mdp : "test123"


INSERT INTO application_user (name, lastname, password, email, country_code, role_name, is_active, is_account_activated, must_change_password)
VALUES
    -- Admin existant - email: anna@smith.com
    ('Anna', 'Smith', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'anna@smith.com', 'US', 'ADMIN', true, true, false),
    -- Nouveaux utilisateurs pour chaque rôle
    ('Pierre', 'Commissaire', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'commissaire@test.com', 'FR', 'COMMISSAIRE', true, true, false),
    ('Marie', 'Athlete', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'athlete@test.com', 'FR', 'ATHLETE', true, true, false),
    ('Jean', 'Volontaire', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'volontaire@test.com', 'FR', 'VOLONTAIRE', true, true, false),
    ('John', 'Doe', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'john@doe.com', 'US', 'ATHLETE', true, true, false),
    ('Jane', 'Smith', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'jane@smith.com', 'US', 'COMMISSAIRE', true, true, false),
    -- Utilisateur non activé pour tester l'activation (mustChangePassword = true, isAccountActivated = false)
    ('New', 'User', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'newuser@test.com', 'FR', 'ATHLETE', true, false, true);
-- ======================
-- Documents
-- ======================
INSERT INTO document (id_doc, file, id_type_doc, id)
VALUES (1, X'010203', 1, 1),
       (2, X'0A0B0C', 2, 2);

-- ======================
-- Privacy Settings (Structure)
-- ======================
CREATE TABLE IF NOT EXISTS privacy_settings (
                                                id SERIAL PRIMARY KEY,
                                                category VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_privacy_user FOREIGN KEY (user_id) REFERENCES application_user(id) ON DELETE CASCADE
    );

-- ======================
-- Teams
-- ======================
INSERT INTO team (name_team, country_code)
VALUES ('Team A', 'FR'),
       ('Team B', 'US'),
       ('Team C', 'DE'),
       ('Team D', 'ES');

-- ======================
-- Membership
-- ======================
INSERT INTO is_a_part_of (id, id_team)
VALUES (1, 1),
       (2, 2);

-- ======================
-- Participation
-- ======================
-- Team participation in trial 2
INSERT INTO participate_at (id_team, id_trial, trial_result_team, is_forfeit)
VALUES (1, 2, '12.4s', false),
       (2, 2, '11.9s', false);

-- Athletes participating in trial 4 and 5
INSERT INTO is_convened_to (id, id_trial, trial_result_athlete, is_forfeit)
VALUES (3, 4, '45.2s', false),
       (4, 4, '46.1s', false),
       (3, 5, '125.3s', false),
       (4, 5, '128.5s', false);

-- ======================
-- Notifications
-- ======================
INSERT INTO notification (id_notification, description_notification, emission_date,
                          id_place, id_event, name_severity, name_type_of_notification)
VALUES (1, 'Event delayed', '2025-01-01 08:00:00', 1, 2, 'WARNING', 'EMAIL');

-- ======================
-- User subscriptions
-- ======================
INSERT INTO subscribe_to (id, id_notification)
VALUES (1, 1);

-- ======================
-- Geolocs
-- ======================
INSERT INTO geoloc (id_geoloc, latitude_geoloc, longitude_geoloc)
VALUES (1, 48.8566, 2.3522);

-- ======================
-- User locations
-- ======================
INSERT INTO can_be_found_at (id, id_geoloc)
VALUES (1, 1);

-- ======================
-- User event schedule
-- ======================
-- Commissaire (id=2) est assigné aux épreuves 1, 2 et 3
INSERT INTO have_a_time_schedule (id, id_event)
VALUES (1, 1),
       (2, 1),
       (2, 2),
       (2, 3);

-- ======================
-- Tasks
-- ======================
INSERT INTO task (id_task, task_name, task_description)
VALUES (1, 'Prepare track', 'Ensure the track surface is clean'),
       (2, 'Check timing system', 'Verify sensors and timing devices');

-- ======================
-- Event-task association
-- ======================
-- Associate task to the trial (which is also an event with id=2)
INSERT INTO is_associated_to (id, id_task)
VALUES (2, 1);

-- ======================
-- User tasks
-- ======================
INSERT INTO must_do (id, id_task)
VALUES (2, 2);
