drop database if exists huffman;
create database huffman;
\c huffman;

CREATE TABLE dico (
    id SERIAL PRIMARY KEY,
    caractere VARCHAR(1) NOT NULL,
    code BIT VARYING NOT NULL
);


SELECT 
    length(caractere) AS taille_bit
FROM dico;
