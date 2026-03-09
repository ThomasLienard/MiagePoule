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
INSERT INTO type_of_document (name_type_doc)
VALUES ('TICKET'),
       ('CEN_ACCREDITATION'),
       ('PASSPORT'),
       ('MEDICAL_CERTIFICATE');

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
-- Type score
-- ======================
INSERT INTO type_score (type_score_name)
VALUES ('TIME'),
       ('POINTS'),
       ('NA');

-- ======================
-- Championships
-- ======================
INSERT INTO championship (description_championship, name_championship,
                          start_date_championship, end_date_championship)
VALUES ('World level championship', 'World Cup', '2025-01-01', '2025-01-02'),
       ('National level championship', 'National League', '2025-01-01', '2025-01-02');

-- ======================
-- Competitions
-- ======================
INSERT INTO competition (name_competition, description_competition,
                         id_championship, start_date_competition, end_date_competition)
VALUES ('100m Sprint', 'Short distance run', 1, '2025-01-01', '2025-01-02'),
       ('Marathon', 'Long distance run', 1, '2025-01-01', '2025-01-02');

-- ======================
-- Places
-- ======================
INSERT INTO place (name_place, city_place, zip_code_place, street_place,
                   parking_place, number_place, description_place,
                   latitude_place, longitude_place)
VALUES ('France Stadium', 'Saint-Denis', '93200', 'Main Street', TRUE, '1',
        'Central stadium', 48.924459, 2.360164),
       ('Bercy Sports Palace', 'Paris', '75012', 'Boulevard de Bercy', FALSE, '8',
        'Indoor sports complex', 48.8365, 2.3738),
       ('Champ de Mars', 'Paris', '75007', 'Avenue de la Bourdonnais', TRUE, '2',
        'Large public greenspace', 48.8550, 2.2980);

-- ======================
-- Time slots
-- ======================
INSERT INTO time_slot (start_time, end_time)
VALUES ('2025-01-01 09:00:00', '2025-01-01 10:00:00'),
       ('2026-01-01 10:00:00', '2026-01-01 11:00:00'),
       ('2026-10-09 09:00:00', '2026-10-09 10:00:00'),
       ('2026-03-02 10:00:00', '2026-03-02 12:00:00'),
       ('2026-02-16 14:25:00', '2026-02-16 16:25:00');

-- ======================
-- Events
-- ======================
INSERT INTO event (name_event, description_event, type_event_name, type_score_name,
                   id_place, id_time_slot, id_competition)
VALUES ('100m Trial Heat 1', 'First qualification heat', 'TRIAL', 'TIME',1, 1, 1),
       ('100m Trial Heat 2', 'Second qualification heat', 'TRIAL', 'TIME', 1, 2, 1),
       ('100m Trial Final', 'Final race', 'TRIAL', 'TIME', 1, 3, 1),
       ('Marathon Trial Warm-up', 'Warm-up session', 'TRIAL', 'TIME', 2, 2, 2),
       ('Marathon Qualification', 'Main qualification heat', 'TRIAL', 'TIME', 2, 1, 2),
       ('Marathon final', 'Main heat', 'TRIAL', 'TIME', 2, 4, 2),
       ('Training Session A', 'Regular training', 'TRAINING','NA', 3, 1, 1),
       ('Training Session B', 'Regular training', 'TRAINING','NA', 3, 2, 1),
       ('Championship Meeting', 'Official gathering', 'MEETING','NA', 3, 3, 2),
        ('Marathon Final', 'Final race', 'TRIAL', 'TIME', 2, 3, 2),
       ('Waterpolo Final', 'Final Match', 'TRIAL', 'POINTS', 2, 3, 2),
        ('200m Sprint Final', 'Finale du 200m sprint — épreuve de démonstration', 'TRIAL', 'TIME', 1, 5, 1);

-- ======================
-- Trials
-- ======================
-- With JOINED inheritance, Trial shares the same primary key with Event
-- Only events with type 'TRIAL' become trials (id 1-5 & 9)
INSERT INTO trial (id_event)
VALUES (1),
       (2),
       (3),
       (4),
       (5),
       (6),
       (10),
       (11),
       (12);

-- ======================
-- Users (MODIFIÉ avec BCrypt)
-- ======================
-- mdp : "test123"


INSERT INTO application_user (name, lastname, password, email, country_code, role_name, is_active, is_account_activated, is_account_validated, must_change_password, created_at, created_by, has_signed_charter)
VALUES
    -- Admin existant - email: anna@smith.com
    ('Anna', 'Smith', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'anna@example.com', 'US', 'ADMIN', true, true, false, false, NOW(), 'system', true),
    -- Nouveaux utilisateurs pour chaque rôle
    ('Pierre', 'Commissaire', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'commissaire@example.com', 'FR', 'COMMISSAIRE', true, true, false, false, NOW(), 'system',true),
    ('Marie', 'Athlete', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'athlete@example.com', 'FR', 'ATHLETE', true, true, false, false, NOW(), 'system', false),
    ('Jean', 'Volontaire', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'volontaire@example.com', 'FR', 'VOLONTAIRE', true, true, false, false, NOW(), 'system', true),
    ('John', 'Doe', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'john@example.com', 'US', 'ATHLETE', true, true, false, false, NOW(), 'system',true),
    ('Jane', 'Smith', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'jane@example.com', 'US', 'COMMISSAIRE', true, true, false, false, NOW(), 'system', true),
    ('Spec', 'tateur', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'spec@example.com', 'US', 'SPECTATEUR', true, true, false, false, NOW(), 'system', true);

-- ======================
-- Documents
-- ======================

-- ======================
-- NE PAS INSÉRER DANS LA TABLE DOCUMENT ICI !
-- ======================

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
-- Team participation: Trial 1 and Trial 2 have team participation
-- Team B est en forfait sur Trial 1 pour les tests
INSERT INTO participate_at (id_team, id_trial, trial_result_team, is_forfeit, is_validated)
VALUES (1, 1, 11.2, false, true),
       (2, 1, null, true, true),
       (1, 2, 11.8, false, true),
       (2,2, 10.9, false, true),
       (2,3, null, false, false);
-- ======================
-- Convened athletes
-- ======================
-- Athletes participation: Trial 4 and Trial 5 have athlete convocation (no participate_at)
INSERT INTO is_convened_to (id, id_trial, trial_result_athlete, is_forfeit, is_validated)
VALUES (1, 4, 8100, false, true),
       (2, 4, 7500, false, true),
       (3, 5, 11.6, false, true),
       (4, 5, 11.2, false, true),
       (3, 10, null, false, true),
       (3, 11, 22.4, false, true),
       (5, 11, 22.1, false, true);

-- ======================
-- Notifications
-- ======================
INSERT INTO notification (description_notification, emission_date,
                          id_place, id_event, name_severity, name_type_of_notification)
VALUES ('Trial 1 starting soon', '2025-01-01 08:30:00', 1, 1, 'WARNING', 'EMAIL'),
       ('Trial 2 delayed', '2025-01-01 09:45:00', 1, 2, 'WARNING', 'SMS'),
       ('Trial 3 finals announcement', '2025-01-01 10:00:00', 1, 3, 'INFO', 'SYSTEM'),
       ('Marathon Trial info', '2025-01-01 08:00:00', 1, 4, 'INFO', 'EMAIL');

-- ======================
-- User subscriptions
-- ======================
INSERT INTO subscribe_to (id, id_notification)
VALUES (1, 1);

-- ======================
-- Geolocs
-- ======================
INSERT INTO geoloc (latitude_geoloc, longitude_geoloc)
VALUES (48.8566, 2.3522);

-- ======================
-- User locations
-- ======================
INSERT INTO can_be_found_at (id, id_geoloc)
VALUES (1, 1);

-- ======================
-- User event schedule
-- ======================
-- Commissaire (id=2) est assigné aux épreuves 1, 2 et 3
-- Jane Smith commissaire (id=6) est assignée aux épreuves 4 et 5
INSERT INTO have_a_time_schedule (id, id_event)
VALUES (1, 1),
       (2, 1),
       (2, 2),
       (2, 3),
       (2, 4),
       (2, 5),
       (2, 6),
       (2, 9),
       (2, 11);

-- ======================
-- Tasks
-- ======================
INSERT INTO task (task_name, task_description)
VALUES ('Prepare track', 'Ensure the track surface is clean'),
       ('Check timing system', 'Verify sensors and timing devices');

-- ======================
-- Event-task association
-- ======================
-- Associate tasks to trials and other events
INSERT INTO is_associated_to (id, id_task)
VALUES (1, 1),
       (2, 1),
       (3, 1),
       (4, 2),
       (5, 2),
       (6, 1),
       (7, 2);

-- ======================
-- User tasks
-- ======================
INSERT INTO must_do (id, id_task)
VALUES (2, 2),
       (3, 1),
       (4, 2);
