INSERT INTO organizaciona_jedinica (oj, naziv) VALUES
('GIRO', 'GIRO-GIRO'),
('Banke u  st', 'Banke u stečaju/likvidaciji'),
('FZI', 'GIRO-FZI'),
('FOD', 'GIRO-FOD');

INSERT INTO users (username, full_name, email, password, org_unit_id, status) VALUES
('adam.adamovic', 'Adam Adamović', 'adam.adamovic@giro.com', '$2b$12$OCmpzwmd9tSOZ2e7cAMVI.AjeKqWyF1B.Foc8EQ6eYIPEnhP1RoCW', (SELECT id FROM organizaciona_jedinica WHERE oj = 'GIRO'), 1),
('marko.markovic', 'Marko Marković', 'marko.markovic@giro.com', '$2b$12$KlkFUq0EJTOnvqU11Z3QTOluG4PJ8Xiaz8XckWjnSaEM5f8r4qHNW', (SELECT id FROM organizaciona_jedinica WHERE oj = 'FZI'), 1),
('jovana.jovanovic', 'Jovana Jovanović', 'jovana.jovanovic@giro.com', '$2b$12$LQRFMoKMPn3jYfzlEN2lUOZTypZFaF02Yhal6hE3fSvKLvLTnfzUi', (SELECT id FROM organizaciona_jedinica WHERE oj = 'FZI'), 1),
('petar.petrovic', 'Petar Petrović', 'petar.petrovic@giro.com', '$2b$12$yeuw44BYOBm/J/hQ212VzuDz7hyV6XHm6nJ.87G7SV/AJrU3T9F6i', (SELECT id FROM organizaciona_jedinica WHERE oj = 'FOD'), 1),
('ana.anic', 'Ana Anić', 'ana.anic@giro.com', '$2b$12$toh3RD0Jn5hzf3e5RvBsTO0OXex6kLMncW5PmQtLxTP64Y7dBRPZ.', (SELECT id FROM organizaciona_jedinica WHERE oj = 'FOD'), 1);

INSERT INTO roles (code, name, description) VALUES
('SUPER_ADMIN', 'Administrator sistema', 'Potpuni pristup sistemu'),
('BUSINESS_ADMIN', 'Biznis admin sistema', 'Šifarnici, depoziti, HoV, kursna lista, izveštaji'),
('DEPOSIT_ADMIN', 'Admin depozita', 'Unos i izmena podataka o depozitima'),
('HOV_ADMIN', 'Admin HoV', 'Unos i izmena podataka o HoV'),
('USER', 'Pregled podataka i izveštaja', 'Pregled šifarnika i pokretanje izveštaja');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'USER'
WHERE u.status <> 2;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'SUPER_ADMIN'
WHERE u.username = 'adam.adamovic'
  AND u.status <> 2;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'BUSINESS_ADMIN'
WHERE u.username = 'marko.markovic'
  AND u.status <> 2;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'DEPOSIT_ADMIN'
WHERE u.username = 'jovana.jovanovic'
  AND u.status <> 2;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'HOV_ADMIN'
WHERE u.username = 'petar.petrovic'
  AND u.status <> 2;
