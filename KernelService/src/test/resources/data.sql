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
INSERT INTO severity (name_severity, desc_severity) VALUES ('INFO', 'Information message');
INSERT INTO severity (name_severity, desc_severity) VALUES ('WARNING', 'Warning level');
INSERT INTO severity (name_severity, desc_severity) VALUES ('CRITICAL', 'Critical event');

-- ======================
-- Type of documents
-- ======================
INSERT INTO type_of_document (name_type_doc) VALUES ('PASSPORT');
INSERT INTO type_of_document (name_type_doc) VALUES ('LICENSE');

-- ======================

-- Type events
-- ======================
INSERT INTO type_event (type_event_name) VALUES ('MEETING');
INSERT INTO type_event (type_event_name) VALUES ('TRAINING');
INSERT INTO type_event (type_event_name) VALUES ('TRIAL');

-- ======================
-- Type score
-- ======================
INSERT INTO type_score (type_score_name) VALUES ('TIME');
INSERT INTO type_score (type_score_name) VALUES ('POINTS');
INSERT INTO type_score (type_score_name) VALUES ('NA');

-- ======================
-- Championships
-- ======================
INSERT INTO championship (description_championship, name_championship, start_date_championship, end_date_championship) VALUES ('World level championship', 'World Cup', '2025-01-01', '2025-01-02');
INSERT INTO championship (description_championship, name_championship, start_date_championship, end_date_championship) VALUES ('National level championship', 'National League', '2025-01-01', '2025-01-02');

-- ======================
-- Competitions
-- ======================
INSERT INTO competition (name_competition, description_competition, id_championship, start_date_competition, end_date_competition) VALUES ('100m Sprint', 'Short distance run', 1, '2025-01-01', '2025-01-02');
INSERT INTO competition (name_competition, description_competition, id_championship, start_date_competition, end_date_competition) VALUES ('Marathon', 'Long distance run', 1, '2025-01-01', '2025-01-02');

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
INSERT INTO time_slot (start_time, end_time) VALUES ('2025-01-01 09:00:00', '2025-01-01 10:00:00');  -- id=1 : passé (épreuve commencée)
INSERT INTO time_slot (start_time, end_time) VALUES ('2026-09-10 09:00:00', '2026-09-10 10:00:00');  -- id=2 : futur (épreuve pas encore commencée)
INSERT INTO time_slot (start_time, end_time) VALUES ('2025-06-15 09:00:00', '2025-06-15 11:00:00');  -- id=3 : passé — épreuves pour tests résultats
-- ======================
-- Events
-- ======================
INSERT INTO event (name_event, description_event, type_event_name, type_score_name, id_place, id_time_slot, id_competition) VALUES ('Morning Sprint Session', 'Speed training', 'TRIAL', 'TIME', 1, 1, 1);
INSERT INTO event (name_event, description_event, type_event_name, type_score_name, id_place, id_time_slot, id_competition) VALUES ('Final Sprint Race', 'Official competition', 'TRIAL', 'TIME', 1, 2, 2);
INSERT INTO event (name_event, description_event, type_event_name, type_score_name, id_place, id_time_slot, id_competition) VALUES ('400m Trial', 'Medium distance race', 'TRIAL', 'TIME', 1, 2, 2);
INSERT INTO event (name_event, description_event, type_event_name, type_score_name, id_place, id_time_slot, id_competition) VALUES ('Team Relay Trial', 'Team competition', 'TRIAL', 'TIME', 1, 2, 2);
INSERT INTO event (name_event, description_event, type_event_name, type_score_name, id_place, id_time_slot, id_competition) VALUES ('Individual 800m Trial', 'Distance race', 'TRIAL', 'TIME', 1, 2, 2);
INSERT INTO event (name_event, description_event, type_event_name, type_score_name, id_place, id_time_slot, id_competition) VALUES ('Past Solo Trial', 'Épreuve solo passée — tests résultats', 'TRIAL', 'POINTS', 1, 3, 1);
INSERT INTO event (name_event, description_event, type_event_name, type_score_name, id_place, id_time_slot, id_competition) VALUES ('Past Team Trial', 'Épreuve équipe passée — tests résultats', 'TRIAL', 'POINTS', 1, 3, 1);

-- ======================
-- Trials
-- ======================
-- With JOINED inheritance, Trial shares the same primary key with Event
-- All events 1-7 are TRIAL type events
INSERT INTO trial (id_event) VALUES (1);  -- Morning Sprint Session
INSERT INTO trial (id_event) VALUES (2);  -- Final Sprint Race
INSERT INTO trial (id_event) VALUES (3);  -- 400m Trial
INSERT INTO trial (id_event) VALUES (4);  -- Team Relay Trial
INSERT INTO trial (id_event) VALUES (5);  -- Individual 800m Trial
INSERT INTO trial (id_event) VALUES (6);  -- Past Solo Trial
INSERT INTO trial (id_event) VALUES (7);  -- Past Team Trial

-- ======================
-- Users (MODIFIÉ avec BCrypt)
-- ======================
-- mdp : "test123"


INSERT INTO application_user (name, lastname, password, email, country_code, role_name, is_active, is_account_activated, must_change_password) VALUES ('Anna', 'Smith', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'anna@smith.com', 'US', 'ADMIN', true, true, false);  -- Admin existant - email: anna@smith.com
INSERT INTO application_user (name, lastname, password, email, country_code, role_name, is_active, is_account_activated, must_change_password) VALUES ('Pierre', 'Commissaire', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'commissaire@test.com', 'FR', 'COMMISSAIRE', true, true, false);  -- Nouveaux utilisateurs pour chaque rôle
INSERT INTO application_user (name, lastname, password, email, country_code, role_name, is_active, is_account_activated, must_change_password) VALUES ('Marie', 'Athlete', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'athlete@test.com', 'FR', 'ATHLETE', true, true, false);
INSERT INTO application_user (name, lastname, password, email, country_code, role_name, is_active, is_account_activated, must_change_password) VALUES ('Jean', 'Volontaire', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'volontaire@test.com', 'FR', 'VOLONTAIRE', true, true, false);
INSERT INTO application_user (name, lastname, password, email, country_code, role_name, is_active, is_account_activated, must_change_password) VALUES ('John', 'Doe', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'john@doe.com', 'US', 'ATHLETE', true, true, false);
INSERT INTO application_user (name, lastname, password, email, country_code, role_name, is_active, is_account_activated, must_change_password) VALUES ('Jane', 'Smith', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'jane@smith.com', 'US', 'COMMISSAIRE', true, true, false);
INSERT INTO application_user (name, lastname, password, email, country_code, role_name, is_active, is_account_activated, must_change_password) VALUES ('New', 'User', '$2a$10$vycWMvbko2wycSl3u6bIL.vCeHgNBQfNq7jpVc7pCEnfER6A2vTLi', 'newuser@test.com', 'FR', 'ATHLETE', true, false, true);  -- Utilisateur non activé pour tester l'activation (mustChangePassword = true, isAccountActivated = false)

-- ======================
-- Privacy Settings (Structure)
-- ======================
-- Note: This table is managed by Hibernate, CREATE TABLE is not needed in data.sql
-- Entities will auto-create the schema

-- ======================
-- Teams
-- ======================
INSERT INTO team (name_team, country_code) VALUES ('Team A', 'FR');
INSERT INTO team (name_team, country_code) VALUES ('Team B', 'US');
INSERT INTO team (name_team, country_code) VALUES ('Team C', 'DE');
INSERT INTO team (name_team, country_code) VALUES ('Team D', 'ES');

-- ======================
-- Membership
-- ======================
INSERT INTO is_a_part_of (id, id_team) VALUES (1, 1);
INSERT INTO is_a_part_of (id, id_team) VALUES (2, 2);

-- ======================
-- Participation
-- ======================
-- Team participation in trial 2
INSERT INTO participate_at (id_team, id_trial, trial_result_team, is_forfeit) VALUES (1, 2, 12.4, false);
INSERT INTO participate_at (id_team, id_trial, trial_result_team, is_forfeit) VALUES (2, 2, 11.9, false);

-- Team participation in past team trial (trial 7) — for result integration tests
INSERT INTO participate_at (id_team, id_trial, trial_result_team, is_forfeit) VALUES (1, 7, null, false);  -- Team A : pas de résultat encore
INSERT INTO participate_at (id_team, id_trial, trial_result_team, is_forfeit) VALUES (2, 7, 11.9, false);  -- Team B : résultat déjà saisi, pas validé

-- Athletes participating in trial 4 and 5
INSERT INTO is_convened_to (id, id_trial, trial_result_athlete, is_forfeit) VALUES (3, 4, 45.2, false);
INSERT INTO is_convened_to (id, id_trial, trial_result_athlete, is_forfeit) VALUES (4, 4, 46.1, false);
INSERT INTO is_convened_to (id, id_trial, trial_result_athlete, is_forfeit) VALUES (3, 5, 125.3, false);
INSERT INTO is_convened_to (id, id_trial, trial_result_athlete, is_forfeit) VALUES (4, 5, 128.5, false);

-- Athletes in past solo trial (trial 6) — for result integration tests
-- Marie (id=3) : pas de résultat, John (id=5) : résultat déjà saisi
INSERT INTO is_convened_to (id, id_trial, trial_result_athlete, is_forfeit) VALUES (3, 6, null, false);
INSERT INTO is_convened_to (id, id_trial, trial_result_athlete, is_forfeit) VALUES (5, 6, 11.5, false);

-- ======================
-- Notifications
-- ======================
INSERT INTO notification (description_notification, emission_date,
                          id_place, id_event, name_severity, type)
VALUES ('Event delayed', '2025-01-01 08:00:00', 1, 2, 'WARNING', 'INFO');

-- ======================
-- User subscriptions
-- ======================
INSERT INTO subscribe_to (id, id_notification) VALUES (1, 1);

-- ======================
-- Geolocs
-- ======================
INSERT INTO geoloc (id_geoloc, latitude_geoloc, longitude_geoloc) VALUES (1, 48.8566, 2.3522);

-- ======================
-- User locations
-- ======================
INSERT INTO can_be_found_at (id, id_geoloc) VALUES (1, 1);

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
INSERT INTO task (id_task, task_name, task_description, id_event)
VALUES (1, 'Prepare track', 'Ensure the track surface is clean', 2),
       (2, 'Check timing system', 'Verify sensors and timing devices', NULL);

-- ======================
-- User tasks
-- ======================
INSERT INTO must_do (id, id_task)
VALUES (2, 2);
