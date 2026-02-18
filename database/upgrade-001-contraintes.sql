-- Mise à niveau d'une base « huffman » existante : ajout des contraintes d'intégrité
-- sans recréer la base. À exécuter une seule fois :
--
--   psql -U postgres -d huffman -f database/upgrade-001-contraintes.sql
--
-- Les ALTER échouent si des doublons existent déjà ; les repérer d'abord avec :
--   SELECT caractere, count(*) FROM dico GROUP BY caractere HAVING count(*) > 1;
--   SELECT code,      count(*) FROM dico GROUP BY code      HAVING count(*) > 1;
-- puis supprimer les lignes en trop (DELETE FROM dico WHERE id = ...).

BEGIN;

ALTER TABLE dico ADD CONSTRAINT dico_caractere_unique  UNIQUE (caractere);
ALTER TABLE dico ADD CONSTRAINT dico_code_unique       UNIQUE (code);
ALTER TABLE dico ADD CONSTRAINT dico_caractere_un_seul CHECK (length(caractere) = 1);
ALTER TABLE dico ADD CONSTRAINT dico_code_non_vide     CHECK (length(code) >= 1);

COMMIT;
