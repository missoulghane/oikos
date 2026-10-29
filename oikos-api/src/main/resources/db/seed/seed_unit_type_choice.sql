-- Types de lot proposés à l'écran, sous une forme rejouable - même raison d'être
-- que seed_reply_medium.sql : le profil dev (H2, Flyway désactivé) n'applique
-- jamais V1__baseline.sql et reçoit ses catalogues par spring.sql.init.data-locations.
-- UnitTypeChoiceSeedTest vérifie que les deux fichiers déclarent le même catalogue.

INSERT INTO unit_type_choice (name, plural, description, position, active)
SELECT 'Appartement', 'appartements', 'Logements de la copropriété.', 1, true
WHERE NOT EXISTS (SELECT 1 FROM unit_type_choice WHERE name = 'Appartement');

INSERT INTO unit_type_choice (name, plural, description, position, active)
SELECT 'Parking', 'parkings', 'Places de stationnement, extérieures ou en sous-sol.', 2, true
WHERE NOT EXISTS (SELECT 1 FROM unit_type_choice WHERE name = 'Parking');

INSERT INTO unit_type_choice (name, plural, description, position, active)
SELECT 'Box', 'box', 'Garages fermés.', 3, true
WHERE NOT EXISTS (SELECT 1 FROM unit_type_choice WHERE name = 'Box');

INSERT INTO unit_type_choice (name, plural, description, position, active)
SELECT 'Bureau', 'bureaux', 'Locaux professionnels.', 4, true
WHERE NOT EXISTS (SELECT 1 FROM unit_type_choice WHERE name = 'Bureau');
