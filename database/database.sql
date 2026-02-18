-- Création de la base « huffman » (PostgreSQL).
-- ATTENTION : détruit la base existante. Pour mettre à niveau une base déjà en place
-- sans perdre les données, utiliser upgrade-001-contraintes.sql.
--
--   psql -U postgres -f database/database.sql

DROP DATABASE IF EXISTS huffman;
CREATE DATABASE huffman;
\c huffman

-- Dictionnaire manuel : un symbole (un point de code) → un code binaire.
-- L'unicité du symbole et du code est garantie ici ; la propriété de préfixe
-- (aucun code préfixe d'un autre) est vérifiée par l'application avant l'insertion.
CREATE TABLE dico (
    id        SERIAL      PRIMARY KEY,
    caractere VARCHAR(1)  NOT NULL,
    code      BIT VARYING NOT NULL,
    CONSTRAINT dico_caractere_unique   UNIQUE (caractere),
    CONSTRAINT dico_code_unique        UNIQUE (code),
    CONSTRAINT dico_caractere_un_seul  CHECK (length(caractere) = 1),
    CONSTRAINT dico_code_non_vide      CHECK (length(code) >= 1)
);
