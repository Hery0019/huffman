-- Création de la base « huffman » (PostgreSQL).
-- ATTENTION : détruit la base existante. Pour mettre à niveau une base déjà en place
-- sans perdre les données, utiliser les scripts upgrade-*.sql dans l'ordre.
--
--   psql -U postgres -f database/database.sql

DROP DATABASE IF EXISTS huffman;
CREATE DATABASE huffman;
\c huffman

-- Un dictionnaire nommé par groupe, par exercice ou par étudiant.
CREATE TABLE dictionnaire (
    id      SERIAL       PRIMARY KEY,
    nom     VARCHAR(50)  NOT NULL,
    cree_le TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT dictionnaire_nom_unique   UNIQUE (nom),
    CONSTRAINT dictionnaire_nom_non_vide CHECK (length(trim(nom)) >= 1)
);

-- Entrées d'un dictionnaire : un symbole (un point de code) → un code binaire.
-- L'unicité du symbole et du code par dictionnaire est garantie ici ; la propriété de préfixe
-- (aucun code préfixe d'un autre) est vérifiée par l'application avant l'insertion.
CREATE TABLE dico (
    id              SERIAL      PRIMARY KEY,
    dictionnaire_id INTEGER     NOT NULL REFERENCES dictionnaire (id) ON DELETE CASCADE,
    caractere       VARCHAR(1)  NOT NULL,
    code            BIT VARYING NOT NULL,
    CONSTRAINT dico_caractere_unique   UNIQUE (dictionnaire_id, caractere),
    CONSTRAINT dico_code_unique        UNIQUE (dictionnaire_id, code),
    CONSTRAINT dico_caractere_un_seul  CHECK (length(caractere) = 1),
    CONSTRAINT dico_code_non_vide      CHECK (length(code) >= 1)
);

INSERT INTO dictionnaire (nom) VALUES ('Principal');
