-- ======================
--  Countries
-- ======================
INSERT INTO Country (Country_code) VALUES ('FR');
INSERT INTO Country (Country_code) VALUES ('US');

-- ======================
--  Roles
-- ======================
INSERT INTO Role (role_name) VALUES ('ADMIN');
INSERT INTO Role (role_name) VALUES ('USER');

-- ======================
--  Severities
-- ======================
INSERT INTO Severity (name_severity, desc_severity)
VALUES ('INFO', 'Information message'),
       ('WARNING', 'Warning level'),
       ('CRITICAL', 'Critical event');

-- ======================
--  Type of documents
-- ======================
INSERT INTO Type_of_document (id_type_doc, name_type_doc)
VALUES (1, 'PASSPORT'),
       (2, 'LICENSE');

-- ======================
--  Type of notification
-- ======================
INSERT INTO Type_of_notification (name_type_of_notification)
VALUES ('EMAIL'),
       ('SMS'),
       ('SYSTEM');

-- ======================
--  Type events
-- ======================
INSERT INTO type_event (type_event_name)
VALUES ('MEETING'),
       ('TRAINING'),
       ('COMPETITION');

-- ======================
--  Championships
-- ======================
INSERT INTO Championship (id_championship, description_championship, name_championship, start_date_championship, end_date_championship)
VALUES (1, 'World level championship', 'World Cup', '2025-01-01','2025-01-02'),
       (2, 'National level championship', 'National League', '2025-01-01','2025-01-02');

-- ======================
--  Competitions
-- ======================
INSERT INTO Competition (id_competition, name_competition, description_competition, id_championship, start_date_competition, end_date_competition)
VALUES (1, '100m Sprint', 'Short distance run', 1, '2025-01-01','2025-01-02'),
       (2, 'Marathon', 'Long distance run', 1, '2025-01-01','2025-01-02');

-- ======================
--  Places
-- ======================
INSERT INTO Place (id_place, name_place, city_place, zip_code_place, street_place, parking_place, number_place,
                   description_place, latitude_place, longitude_place)
VALUES (1, 'Olympic Stadium', 'Paris', '75000', 'Main Street', TRUE, '10',
        'Central stadium', 48.85, 2.35);

-- ======================
--  Time slots
-- ======================
INSERT INTO Time_slot (id_time_slot, start_time, end_time)
VALUES (1, '2025-01-01 09:00:00', '2025-01-01 10:00:00'),
       (2, '2025-01-01 10:00:00', '2025-01-01 11:00:00');

-- ======================
--  Events
-- ======================
INSERT INTO Event (id_event, name_event, description_event, type_event_name, id_place, id_time_slot, id_competition)
VALUES (1, 'Morning Sprint Session', 'Speed training', 'TRAINING', 1, 1, 1),
       (2, 'Final Sprint Race', 'Official competition', 'COMPETITION', 1, 2, 2);

-- ======================
--  Trials
-- ======================
INSERT INTO Trial (id_trial, id_event)
VALUES (1, 2);

-- ======================
--  Users
-- ======================
INSERT INTO Application_user (id, name, lastname, password, email, Country_code, role_name)
VALUES (1, 'John', 'Doe', 'password', 'john@doe.com', 'FR', 'USER'),
       (2, 'Anna', 'Smith', 'password', 'anna@smith.com', 'US', 'ADMIN');

-- ======================
--  Documents
-- ======================
INSERT INTO Document (id_doc, file, id_type_doc, id)
VALUES (1, X'010203', 1, 1),
       (2, X'0A0B0C', 2, 2);

-- ======================
--  Teams
-- ======================
INSERT INTO Team (id_team, name_team, Country_code)
VALUES (1, 'Team A', 'FR'),
       (2, 'Team B', 'US');

-- ======================
--  Membership
-- ======================
INSERT INTO is_a_part_of (id, id_team)
VALUES (1, 1),
       (2, 2);

-- ======================
--  Participation
-- ======================
INSERT INTO participate_at (id_team, id_trial, trial_result_team)
VALUES (1, 1, '12.4s'),
       (2, 1, '11.9s');

-- ======================
--  Convened athletes
-- ======================
INSERT INTO is_convened_to (id, id_trial, trial_result_athlete)
VALUES (1, 1, '12.4s');

-- ======================
--  Notifications
-- ======================
INSERT INTO Notification (id_notification, description_notification, emission_date,
                          id_place, id_event, name_severity, name_type_of_notification)
VALUES (1, 'Event delayed', '2025-01-01 08:00:00', 1, 2, 'WARNING', 'EMAIL');

-- ======================
--  User subscriptions
-- ======================
INSERT INTO Subscribe_to (id, id_notification)
VALUES (1, 1);

-- ======================
--  Geolocs
-- ======================
INSERT INTO geoloc (id_geoloc, latitude_geoloc, longitude_geoloc)
VALUES (1, 48.8566, 2.3522);

-- ======================
--  User locations
-- ======================
INSERT INTO can_be_found_at (id, id_geoloc)
VALUES (1, 1);

-- ======================
--  User event schedule
-- ======================
INSERT INTO have_a_time_schedule (id, id_event)
VALUES (1, 1);

-- ======================
--  Tasks
-- ======================
INSERT INTO Task (id_task, task_name, task_description)
VALUES (1, 'Prepare track', 'Ensure the track surface is clean'),
       (2, 'Check timing system', 'Verify sensors and timing devices');

-- ======================
--  Event-task association
-- ======================
INSERT INTO is_associated_to (id_event, id_task)
VALUES (2, 1);

-- ======================
--  User tasks
-- ======================
INSERT INTO must_do (id, id_task)
VALUES (2, 2);
