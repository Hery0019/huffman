-- Mise à niveau : dictionnaires nommés. Les entrées existantes rejoignent un dictionnaire « Principal ».
-- Prérequis : upgrade-001-contraintes.sql appliqué (ou base créée avec ses contraintes).
-- À exécuter une seule fois :
--
--   psql -U postgres -d huffman -f database/upgrade-002-dictionnaires.sql

BEGIN;

CREATE TABLE dictionnaire (
    id      SERIAL       PRIMARY KEY,
    nom     VARCHAR(50)  NOT NULL,
    cree_le TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT dictionnaire_nom_unique   UNIQUE (nom),
    CONSTRAINT dictionnaire_nom_non_vide CHECK (length(trim(nom)) >= 1)
);

INSERT INTO dictionnaire (nom) VALUES ('Principal');

ALTER TABLE dico ADD COLUMN dictionnaire_id INTEGER;
UPDATE dico SET dictionnaire_id = (SELECT id FROM dictionnaire WHERE nom = 'Principal');
ALTER TABLE dico ALTER COLUMN dictionnaire_id SET NOT NULL;
ALTER TABLE dico ADD CONSTRAINT dico_dictionnaire_fk
    FOREIGN KEY (dictionnaire_id) REFERENCES dictionnaire (id) ON DELETE CASCADE;

-- L'unicité devient relative au dictionnaire.
ALTER TABLE dico DROP CONSTRAINT dico_caractere_unique;
ALTER TABLE dico DROP CONSTRAINT dico_code_unique;
ALTER TABLE dico ADD CONSTRAINT dico_caractere_unique UNIQUE (dictionnaire_id, caractere);
ALTER TABLE dico ADD CONSTRAINT dico_code_unique      UNIQUE (dictionnaire_id, code);

COMMIT;
